@file:JvmSynthetic

package com.walletconnect.notify.engine.requests

import com.walletconnect.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.walletconnect.android.internal.common.model.AccountId
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.walletconnect.android.internal.common.model.params.CoreNotifyParams
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.reown.foundation.common.model.Ttl
import com.reown.foundation.util.Logger
import com.reown.foundation.util.jwt.decodeDidPkh
import com.reown.foundation.util.jwt.decodeEd25519DidKey
import com.walletconnect.notify.common.NotifyServerUrl
import com.walletconnect.notify.common.model.SubscriptionChanged
import com.walletconnect.notify.data.jwt.subscriptionsChanged.SubscriptionsChangedRequestJwtClaim
import com.walletconnect.notify.data.storage.RegisteredAccountsRepository
import com.walletconnect.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import com.walletconnect.notify.engine.domain.FetchDidJwtInteractor
import com.walletconnect.notify.engine.domain.SetActiveSubscriptionsUseCase
import com.walletconnect.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSubscriptionsChangedUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val fetchDidJwtInteractor: FetchDidJwtInteractor,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val registeredAccountsRepository: RegisteredAccountsRepository,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val watchSubscriptionsForEveryRegisteredAccountUseCase: WatchSubscriptionsForEveryRegisteredAccountUseCase,
    private val logger: Logger,
    private val notifyServerUrl: NotifyServerUrl,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: CoreNotifyParams.SubscriptionsChangedParams) = supervisorScope {
        try {
            val jwtClaims = extractVerifiedDidJwtClaims<SubscriptionsChangedRequestJwtClaim>(params.subscriptionsChangedAuth).getOrThrow()
            logger.log("SubscriptionsChangedRequestJwtClaim: ${decodeEd25519DidKey(jwtClaims.audience).keyAsHex} - $jwtClaims")
            val authenticationPublicKey = registeredAccountsRepository.getAccountByIdentityKey(decodeEd25519DidKey(jwtClaims.audience).keyAsHex).notifyServerAuthenticationKey
                ?: throw IllegalStateException("Cached authentication public key is null")

            jwtClaims.throwIfIsInvalid(authenticationPublicKey.keyAsHex)

            val account = decodeDidPkh(jwtClaims.subject)
            val subscriptions = setActiveSubscriptionsUseCase(account, jwtClaims.subscriptions).getOrThrow()
            val didJwt = fetchDidJwtInteractor.subscriptionsChangedResponse(AccountId(account), authenticationPublicKey).getOrThrow()
            val responseParams = ChatNotifyResponseAuthParams.ResponseAuth(didJwt.value)
            val irnParams = IrnParams(Tags.NOTIFY_SUBSCRIPTIONS_CHANGED_RESPONSE, Ttl(fiveMinutesInSeconds))

            jsonRpcInteractor.respondWithParams(request.id, request.topic, responseParams, irnParams, onFailure = { error -> logger.error(error) })

            _events.emit(SubscriptionChanged(subscriptions))
        } catch (error: Throwable) {
            logger.error(error)
            _events.emit(SDKError(error))
        }
    }


    private suspend fun SubscriptionsChangedRequestJwtClaim.throwIfIsInvalid(expectedIssuerAsHex: String) {
        throwIfBaseIsInvalid()
        throwIfAudienceAndIssuerIsInvalidAndRetriggerWatchingLogicOnOutdatedIssuer(expectedIssuerAsHex)
    }

    private suspend fun SubscriptionsChangedRequestJwtClaim.throwIfAudienceAndIssuerIsInvalidAndRetriggerWatchingLogicOnOutdatedIssuer(expectedIssuerAsHex: String) {

        val decodedIssuerAsHex = decodeEd25519DidKey(issuer).keyAsHex
        if (decodedIssuerAsHex != expectedIssuerAsHex) {
            val (_, newAuthenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(notifyServerUrl.toUri()).getOrThrow()

            if (decodedIssuerAsHex == newAuthenticationPublicKey.keyAsHex)
                watchSubscriptionsForEveryRegisteredAccountUseCase()
            else
                throw IllegalStateException("Issuer $decodedIssuerAsHex is not valid with cached $expectedIssuerAsHex or fresh ${newAuthenticationPublicKey.keyAsHex}")
        }
    }
}