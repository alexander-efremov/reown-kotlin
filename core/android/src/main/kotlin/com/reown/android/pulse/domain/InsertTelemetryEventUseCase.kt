package com.reown.android.pulse.domain

import com.reown.android.internal.common.storage.events.EventsRepository
import com.reown.android.pulse.model.properties.Props
import com.reown.foundation.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface InsertEventUseCaseInterface {
    suspend operator fun invoke(props: Props)
}

class InsertTelemetryEventUseCase(
    private val eventsRepository: EventsRepository,
    private val logger: Logger
) : InsertEventUseCaseInterface {
    override suspend operator fun invoke(props: Props) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.insertOrAbortTelemetry(props)
            } catch (e: Exception) {
                logger.error("Inserting event ${props.type} error: $e")
            }
        }
    }
}

class InsertEventUseCase(
    private val eventsRepository: EventsRepository,
    private val logger: Logger
) : InsertEventUseCaseInterface {
    override suspend operator fun invoke(props: Props) {
        withContext(Dispatchers.IO) {
            try {
                eventsRepository.insertOrAbort(props)
            } catch (e: Exception) {
                logger.error("Inserting event ${props.type} error: $e")
            }
        }
    }
}