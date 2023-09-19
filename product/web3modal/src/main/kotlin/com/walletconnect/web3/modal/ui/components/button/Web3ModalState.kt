@file:OptIn(ExperimentalCoroutinesApi::class)

package com.walletconnect.web3.modal.ui.components.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.walletconnect.android.internal.common.wcKoinApp
import com.walletconnect.android.internal.utils.CoreValidator
import com.walletconnect.util.Empty
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.Web3Modal
import com.walletconnect.web3.modal.domain.delegate.Web3ModalDelegate
import com.walletconnect.web3.modal.domain.model.Chain
import com.walletconnect.web3.modal.domain.usecase.GetSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.GetSessionTopicUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSessionTopicUseCase
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.openWeb3Modal
import com.walletconnect.web3.modal.utils.getAddress
import com.walletconnect.web3.modal.utils.getChains
import com.walletconnect.web3.modal.utils.getSelectedChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun rememberWeb3ModalState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController
): Web3ModalState {
    return remember(navController) {
        Web3ModalState(coroutineScope, navController)
    }
}

class Web3ModalState(
    private val coroutineScope: CoroutineScope,
    private val navController: NavController
) {
    private val observeSessionTopicUseCase: ObserveSessionTopicUseCase = wcKoinApp.koin.get()
    private val getSessionTopicUseCase: GetSessionTopicUseCase = wcKoinApp.koin.get()
    private val getSelectedChainUseCase: GetSelectedChainUseCase = wcKoinApp.koin.get()


    val isOpen = navController.currentBackStackEntryFlow.mapLatest { it.destination.route?.startsWith(Route.WEB3MODAL.path) ?: false }

    private val sessionTopicFlow = observeSessionTopicUseCase()

    val isConnected = sessionTopicFlow
        .map { it != null && Web3Modal.getActiveSessionByTopic(it) != null }
        .stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = false)

    internal val accountNormalButtonState = sessionTopicFlow
        .mapOrAccountState(AccountButtonType.NORMAL)
        .stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = AccountButtonState.Loading)

    internal val accountMixedButtonState = sessionTopicFlow
        .mapOrAccountState(AccountButtonType.MIXED)
        .stateIn(coroutineScope, started = SharingStarted.Lazily, initialValue = AccountButtonState.Loading)

    private fun Flow<String?>.mapOrAccountState(accountButtonType: AccountButtonType) =
        map { topic -> topic?.let { getActiveSession()?.mapToAccountButtonState(accountButtonType) } ?: AccountButtonState.Invalid }


    private suspend fun Modal.Model.Session.mapToAccountButtonState(accountButtonType: AccountButtonType) = try {
        val chains = getChains()
        val selectedChain = chains.getSelectedChain(getSelectedChainUseCase())
        val address = getAddress(selectedChain)
        when (accountButtonType) {
            AccountButtonType.NORMAL -> AccountButtonState.Normal(address = address)
            AccountButtonType.MIXED -> AccountButtonState.Mixed(
                address = address,
                chainImageUrl = selectedChain.imageUrl,
                chainName = selectedChain.name
            )
        }
    } catch (e: Exception) {
        AccountButtonState.Invalid
    }

    private suspend fun getActiveSession() = getSessionTopicUseCase()?.let { Web3Modal.getActiveSessionByTopic(it) }

    internal fun getSelectedChain() = runBlocking { getSelectedChainUseCase()?.let { Chain(it) } }

    internal fun openWeb3Modal() {
        navController.openWeb3Modal()
    }
}
