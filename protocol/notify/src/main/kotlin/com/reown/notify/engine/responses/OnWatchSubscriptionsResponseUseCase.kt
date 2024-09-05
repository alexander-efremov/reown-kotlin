@file:JvmSynthetic

package com.reown.notify.engine.responses

import com.reown.android.internal.common.JsonRpcResponse
import com.reown.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.reown.android.internal.common.model.SDKError
import com.reown.android.internal.common.model.WCResponse
import com.reown.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.reown.android.internal.common.model.type.EngineEvent
import com.reown.foundation.util.Logger
import com.reown.foundation.util.jwt.decodeDidPkh
import com.reown.foundation.util.jwt.decodeEd25519DidKey
import com.reown.notify.common.NotifyServerUrl
import com.reown.notify.common.model.SubscriptionChanged
import com.reown.notify.data.jwt.watchSubscriptions.WatchSubscriptionsResponseJwtClaim
import com.reown.notify.data.storage.RegisteredAccountsRepository
import com.reown.notify.engine.domain.ExtractPublicKeysFromDidJsonUseCase
import com.reown.notify.engine.domain.SetActiveSubscriptionsUseCase
import com.reown.notify.engine.domain.WatchSubscriptionsForEveryRegisteredAccountUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnWatchSubscriptionsResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val extractPublicKeysFromDidJsonUseCase: ExtractPublicKeysFromDidJsonUseCase,
    private val watchSubscriptionsForEveryRegisteredAccountUseCase: WatchSubscriptionsForEveryRegisteredAccountUseCase,
    private val accountsRepository: RegisteredAccountsRepository,
    private val notifyServerUrl: NotifyServerUrl,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val jwtClaims = extractVerifiedDidJwtClaims<WatchSubscriptionsResponseJwtClaim>(responseAuth).getOrThrow()

                    jwtClaims.throwIfIsInvalid()

                    val subscriptions = setActiveSubscriptionsUseCase(decodeDidPkh(jwtClaims.subject), jwtClaims.subscriptions).getOrThrow()
                    SubscriptionChanged(subscriptions)
                }

                is JsonRpcResponse.JsonRpcError -> {
                    SDKError(Exception(response.errorMessage))
                }
            }
        } catch (e: Exception) {
            logger.error(e)
            SDKError(e)
        }

        _events.emit(resultEvent)
    }

    private suspend fun WatchSubscriptionsResponseJwtClaim.throwIfIsInvalid() {
        throwIfBaseIsInvalid()
        throwIfAudienceAndIssuerIsInvalidAndRetriggerWatchingLogicOnOutdatedIssuer()
    }

    private suspend fun WatchSubscriptionsResponseJwtClaim.throwIfAudienceAndIssuerIsInvalidAndRetriggerWatchingLogicOnOutdatedIssuer() {
        val expectedIssuer = runCatching { accountsRepository.getAccountByIdentityKey(decodeEd25519DidKey(audience).keyAsHex).notifyServerAuthenticationKey }
            .getOrElse { throw IllegalStateException("Account does not exist in storage for $audience") } ?: throw IllegalStateException("Cached authentication public key is null")

        val decodedIssuerAsHex = decodeEd25519DidKey(issuer).keyAsHex
        if (decodedIssuerAsHex != expectedIssuer.keyAsHex) {
            val (_, newAuthenticationPublicKey) = extractPublicKeysFromDidJsonUseCase(notifyServerUrl.toUri()).getOrThrow()

            if (decodedIssuerAsHex == newAuthenticationPublicKey.keyAsHex)
                watchSubscriptionsForEveryRegisteredAccountUseCase()
            else
                throw IllegalStateException("Issuer $decodedIssuerAsHex is not valid with cached ${expectedIssuer.keyAsHex} or fresh ${newAuthenticationPublicKey.keyAsHex}")
        }
    }
}