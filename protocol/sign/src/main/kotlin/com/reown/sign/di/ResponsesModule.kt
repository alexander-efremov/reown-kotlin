package com.reown.sign.di

import com.reown.android.internal.common.di.AndroidCommonDITags
import com.reown.android.pulse.domain.InsertEventUseCase
import com.reown.sign.engine.use_case.responses.OnSessionAuthenticateResponseUseCase
import com.reown.sign.engine.use_case.responses.OnSessionProposalResponseUseCase
import com.reown.sign.engine.use_case.responses.OnSessionRequestResponseUseCase
import com.reown.sign.engine.use_case.responses.OnSessionSettleResponseUseCase
import com.reown.sign.engine.use_case.responses.OnSessionUpdateResponseUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

@JvmSynthetic
internal fun responsesModule() = module {

    single {
        OnSessionProposalResponseUseCase(
            jsonRpcInteractor = get(),
            crypto = get(),
            pairingController = get(),
            proposalStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnSessionSettleResponseUseCase(
            crypto = get(),
            jsonRpcInteractor = get(),
            sessionStorageRepository = get(),
            metadataStorageRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER))
        )
    }

    single {
        OnSessionAuthenticateResponseUseCase(
            pairingController = get(),
            pairingInterface = get(),
            cacaoVerifier = get(),
            sessionStorageRepository = get(),
            crypto = get(),
            jsonRpcInteractor = get(),
            authenticateResponseTopicRepository = get(),
            logger = get(named(AndroidCommonDITags.LOGGER)),
            getSessionAuthenticateRequest = get(),
            metadataStorageRepository = get(),
            linkModeStorageRepository = get(),
            insertEventUseCase = get<InsertEventUseCase>(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
        )
    }

    single { OnSessionUpdateResponseUseCase(sessionStorageRepository = get(), logger = get(named(AndroidCommonDITags.LOGGER))) }

    single {
        OnSessionRequestResponseUseCase(
            logger = get(named(AndroidCommonDITags.LOGGER)),
            insertEventUseCase = get<InsertEventUseCase>(),
            clientId = get(named(AndroidCommonDITags.CLIENT_ID)),
            getSessionRequestByIdUseCase = get()
        )
    }
}