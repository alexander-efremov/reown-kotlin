package com.walletconnect.sign.engine.use_case.requests

import com.walletconnect.android.internal.common.exception.Uncategorized
import com.walletconnect.android.internal.common.model.IrnParams
import com.walletconnect.android.internal.common.model.SDKError
import com.walletconnect.android.internal.common.model.Tags
import com.walletconnect.android.internal.common.model.WCRequest
import com.walletconnect.android.internal.common.model.type.EngineEvent
import com.walletconnect.android.internal.common.model.type.JsonRpcInteractorInterface
import com.walletconnect.android.internal.utils.fiveMinutesInSeconds
import com.walletconnect.foundation.common.model.Ttl
import com.walletconnect.foundation.util.Logger
import com.walletconnect.sign.common.exceptions.PeerError
import com.walletconnect.sign.common.model.type.Sequences
import com.walletconnect.sign.common.model.vo.clientsync.session.params.SignParams
import com.walletconnect.sign.common.validator.SignValidator
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDOEvent
import com.walletconnect.sign.engine.model.mapper.toPeerError
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.supervisorScope

internal class OnSessionEventUseCase(
    private val jsonRpcInteractor: JsonRpcInteractorInterface,
    private val sessionStorageRepository: SessionStorageRepository,
    private val logger: Logger
) {
    private val _events: MutableSharedFlow<EngineEvent> = MutableSharedFlow()
    val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    suspend operator fun invoke(request: WCRequest, params: SignParams.EventParams) = supervisorScope {
        logger.log("Session event received on topic: ${request.topic}")
        val irnParams = IrnParams(Tags.SESSION_EVENT_RESPONSE, Ttl(fiveMinutesInSeconds))
        try {
            SignValidator.validateEvent(params.toEngineDOEvent()) { error ->
                logger.error("Session event received failure on topic: ${request.topic} - $error")
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return@supervisorScope
            }

            if (!sessionStorageRepository.isSessionValid(request.topic)) {
                logger.error("Session event received failure on topic: ${request.topic} - invalid session")
                jsonRpcInteractor.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
                return@supervisorScope
            }

            val session = sessionStorageRepository.getSessionWithoutMetadataByTopic(request.topic)
            if (!session.isPeerController) {
                logger.error("Session event received failure on topic: ${request.topic} - unauthorized peer")
                jsonRpcInteractor.respondWithError(request, PeerError.Unauthorized.Event(Sequences.SESSION.name), irnParams)
                return@supervisorScope
            }
            if (!session.isAcknowledged) {
                logger.error("Session event received failure on topic: ${request.topic} - no matching topic")
                jsonRpcInteractor.respondWithError(request, Uncategorized.NoMatchingTopic(Sequences.SESSION.name, request.topic.value), irnParams)
                return@supervisorScope
            }

            val event = params.event
            SignValidator.validateChainIdWithEventAuthorisation(params.chainId, event.name, session.sessionNamespaces) { error ->
                logger.error("Session event received failure on topic: ${request.topic} - $error")
                jsonRpcInteractor.respondWithError(request, error.toPeerError(), irnParams)
                return@supervisorScope
            }

            jsonRpcInteractor.respondWithSuccess(request, irnParams)
            logger.log("Session event received on topic: ${request.topic} - emitting")
            _events.emit(params.toEngineDO(request.topic))
        } catch (e: Exception) {
            logger.error("Session event received failure on topic: ${request.topic} - $e")
            jsonRpcInteractor.respondWithError(request, Uncategorized.GenericError("Cannot emit an event: ${e.message}, topic: ${request.topic}"), irnParams)
            _events.emit(SDKError(e))
            return@supervisorScope
        }
    }
}