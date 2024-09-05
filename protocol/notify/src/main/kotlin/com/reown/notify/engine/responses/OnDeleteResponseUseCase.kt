@file:JvmSynthetic

package com.reown.notify.engine.responses

import com.reown.android.internal.common.JsonRpcResponse
import com.reown.android.internal.common.jwt.did.extractVerifiedDidJwtClaims
import com.reown.android.internal.common.model.WCResponse
import com.reown.android.internal.common.model.params.ChatNotifyResponseAuthParams
import com.reown.android.internal.common.model.params.CoreNotifyParams
import com.reown.android.internal.common.model.type.EngineEvent
import com.reown.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.reown.foundation.util.Logger
import com.reown.foundation.util.jwt.decodeDidPkh
import com.reown.notify.common.model.DeleteSubscription
import com.reown.notify.data.jwt.delete.DeleteResponseJwtClaim
import com.reown.notify.data.storage.NotificationsRepository
import com.reown.notify.engine.domain.SetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnDeleteResponseUseCase(
    private val setActiveSubscriptionsUseCase: SetActiveSubscriptionsUseCase,
    private val jsonRpcInteractor: RelayJsonRpcInteractorInterface,
    private val notificationsRepository: NotificationsRepository,
    private val logger: Logger,
) {
    private val _events: MutableSharedFlow<Pair<CoreNotifyParams.DeleteParams, EngineEvent>> = MutableSharedFlow()
    val events: SharedFlow<Pair<CoreNotifyParams.DeleteParams, EngineEvent>> = _events.asSharedFlow()

    suspend operator fun invoke(wcResponse: WCResponse, params: CoreNotifyParams.DeleteParams) = supervisorScope {
        val resultEvent = try {
            when (val response = wcResponse.response) {
                is JsonRpcResponse.JsonRpcResult -> {
                    val responseAuth = (response.result as ChatNotifyResponseAuthParams.ResponseAuth).responseAuth
                    val responseJwtClaim = extractVerifiedDidJwtClaims<DeleteResponseJwtClaim>(responseAuth).getOrThrow()
                    responseJwtClaim.throwIfBaseIsInvalid()

                    jsonRpcInteractor.unsubscribe(wcResponse.topic)

                    notificationsRepository.deleteNotificationsByTopic(wcResponse.topic.value)
                    setActiveSubscriptionsUseCase(decodeDidPkh(responseJwtClaim.subject), responseJwtClaim.subscriptions).getOrThrow()

                    DeleteSubscription.Success(wcResponse.topic.value)
                }

                is JsonRpcResponse.JsonRpcError -> DeleteSubscription.Error(Throwable(response.error.message))
            }
        } catch (e: Exception) {
            logger.error(e)
            DeleteSubscription.Error(e)
        }

        _events.emit(params to resultEvent)
    }
}