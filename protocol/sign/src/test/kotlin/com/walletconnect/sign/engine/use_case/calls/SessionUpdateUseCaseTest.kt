package com.walletconnect.sign.engine.use_case.calls

import com.reown.android.internal.common.exception.CannotFindSequenceForTopic
import com.reown.android.internal.common.model.type.RelayJsonRpcInteractorInterface
import com.reown.foundation.util.Logger
import com.walletconnect.sign.storage.sequence.SessionStorageRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class SessionUpdateUseCaseTest {
    private val sessionStorageRepository = mockk<SessionStorageRepository>()
    private val jsonRpcInteractor = mockk<RelayJsonRpcInteractorInterface>()
    private val logger = mockk<Logger>()
    private val sessionUpdateUseCase = SessionUpdateUseCase(jsonRpcInteractor, sessionStorageRepository, logger)

    @Before
    fun setUp() {
        every { logger.error(any() as String) } answers { }
        every { logger.error(any() as Exception) } answers { }
    }

    @Test
    fun `onFailure is called when sessionStorageRepository isSessionValid is false`() = runTest {
        every { sessionStorageRepository.isSessionValid(any()) } returns false

        sessionUpdateUseCase.sessionUpdate(
            topic = "topic",
            namespaces = emptyMap(),
            onSuccess = {
                fail("onSuccess should not be called since should have validation failed")
            },
            onFailure = { error ->
                assertSame(CannotFindSequenceForTopic::class, error::class)
            }
        )
    }
}