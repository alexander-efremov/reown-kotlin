package com.walletconnect.sign.engine.use_case.calls

import com.reown.android.internal.utils.CoreValidator.isExpired
import com.walletconnect.sign.common.model.vo.proposal.ProposalVO
import com.walletconnect.sign.engine.model.EngineDO
import com.walletconnect.sign.engine.model.mapper.toEngineDO
import com.walletconnect.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.supervisorScope

internal class GetSessionProposalsUseCase(private val proposalStorageRepository: ProposalStorageRepository) : GetSessionProposalsUseCaseInterface {
    override suspend fun getSessionProposals(): List<EngineDO.SessionProposal> =
        supervisorScope {
            proposalStorageRepository.getProposals().filter { proposal -> proposal.expiry?.let { !it.isExpired() } ?: true }.map(ProposalVO::toEngineDO)
        }
}

internal interface GetSessionProposalsUseCaseInterface {
    suspend fun getSessionProposals(): List<EngineDO.SessionProposal>
}