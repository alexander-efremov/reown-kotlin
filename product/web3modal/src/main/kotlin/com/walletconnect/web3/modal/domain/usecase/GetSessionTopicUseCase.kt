package com.walletconnect.web3.modal.domain.usecase

import com.walletconnect.web3.modal.domain.SessionRepository

internal class GetSessionTopicUseCase(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() = repository.getSessionTopic()
}