package com.walletconnect.web3.modal.ui.routes.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reown.android.internal.common.wcKoinApp
import com.reown.android.pulse.domain.SendEventInterface
import com.reown.android.pulse.model.EventType
import com.reown.android.pulse.model.properties.Properties
import com.reown.android.pulse.model.properties.Props
import com.reown.foundation.util.Logger
import com.reown.modal.ui.model.UiState
import com.walletconnect.web3.modal.client.Modal
import com.walletconnect.web3.modal.client.models.request.Request
import com.walletconnect.web3.modal.client.models.request.SentRequestResult
import com.walletconnect.web3.modal.domain.model.AccountData
import com.walletconnect.web3.modal.domain.model.Session
import com.walletconnect.web3.modal.domain.usecase.GetEthBalanceUseCase
import com.walletconnect.web3.modal.domain.usecase.GetIdentityUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSelectedChainUseCase
import com.walletconnect.web3.modal.domain.usecase.ObserveSessionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveChainSelectionUseCase
import com.walletconnect.web3.modal.domain.usecase.SaveSessionUseCase
import com.walletconnect.web3.modal.engine.AppKitEngine
import com.walletconnect.web3.modal.engine.coinbase.CoinbaseResult
import com.walletconnect.web3.modal.ui.navigation.Navigator
import com.walletconnect.web3.modal.ui.navigation.NavigatorImpl
import com.walletconnect.web3.modal.ui.navigation.Route
import com.walletconnect.web3.modal.ui.navigation.account.toChainSwitchPath
import com.walletconnect.web3.modal.utils.EthUtils
import com.walletconnect.web3.modal.utils.createAddEthChainParams
import com.walletconnect.web3.modal.utils.createSwitchChainParams
import com.walletconnect.web3.modal.utils.getChains
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class AccountViewModel : ViewModel(), Navigator by NavigatorImpl() {
    private val logger: Logger = wcKoinApp.koin.get()

    private val saveChainSelectionUseCase: SaveChainSelectionUseCase = wcKoinApp.koin.get()
    private val saveSessionUseCase: SaveSessionUseCase = wcKoinApp.koin.get()
    private val observeSessionUseCase: ObserveSessionUseCase = wcKoinApp.koin.get()
    private val observeSelectedChainUseCase: ObserveSelectedChainUseCase = wcKoinApp.koin.get()
    private val getIdentityUseCase: GetIdentityUseCase = wcKoinApp.koin.get()
    private val getEthBalanceUseCase: GetEthBalanceUseCase = wcKoinApp.koin.get()
    private val appKitEngine: AppKitEngine = wcKoinApp.koin.get()
    private val sendEventUseCase: SendEventInterface = wcKoinApp.koin.get()
    private val activeSessionFlow = observeSessionUseCase()

    private val accountDataFlow = activeSessionFlow
        .map {
            if (appKitEngine.getAccount() != null) {
                it
            } else {
                null
            }
        }
        .map { activeSession ->
            if (activeSession != null) {
                val chains = activeSession.getChains()
                val identity = getIdentityUseCase(activeSession.address, activeSession.chain)
                accountData = AccountData(
                    address = activeSession.address, chains = chains, identity = identity
                )
                UiState.Success(accountData)
            } else {
                UiState.Error(Throwable("Active session not found"))
            }
        }.catch {
            showError(it.localizedMessage)
            logger.error(it)
            emit(UiState.Error(it))
        }

    lateinit var accountData: AccountData

    val accountState = accountDataFlow.stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = UiState.Loading())

    val selectedChain = observeSelectedChainUseCase().map { appKitEngine.getSelectedChainOrFirst() }

    val balanceState = combine(activeSessionFlow, selectedChain) { session, selectedChain ->
        if (session != null && selectedChain.rpcUrl != null) {
            return@combine getEthBalanceUseCase(selectedChain.token, selectedChain.rpcUrl, session.address)
        } else {
            null
        }
    }.flowOn(Dispatchers.IO).catch { logger.error(it) }.stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)

    fun disconnect() {
        appKitEngine.disconnect(
            onSuccess = { closeModal() },
            onError = {
                showError(it.localizedMessage)
                logger.error(it)
            }
        )
    }

    fun changeActiveChain(chain: Modal.Model.Chain) = viewModelScope.launch {
        if (accountData.chains.contains(chain)) {
            sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.SWITCH_NETWORK, Properties(network = chain.id)))
            saveChainSelectionUseCase(chain.id)
            popBackStack()
        } else {
            navigateTo(chain.toChainSwitchPath())
        }
    }

    suspend fun updatedSessionAfterChainSwitch(updatedSession: Session) {
        if (updatedSession.getChains().any { it.id == updatedSession.chain }) {
            sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.SWITCH_NETWORK, Properties(network = updatedSession.chain)))
            saveSessionUseCase(updatedSession)
            popBackStack(path = Route.CHANGE_NETWORK.path, inclusive = true)
        }
    }

    fun switchChain(to: Modal.Model.Chain, onReject: () -> Unit) {
        val onError: (String?) -> Unit = { showError(it ?: "Something went wrong") }
        val isChainApproved = accountData.chains.contains(to)
        val onSuccess: (SentRequestResult) -> Unit = { it.handleRequestResult(to, onError, onReject) }
        if (!isChainApproved && to.optionalMethods.contains(EthUtils.walletAddEthChain)) {
            addEthChain(to, onSuccess, onError)
        } else {
            switchEthChain(to, onSuccess, onError)
        }
    }

    private fun SentRequestResult.handleRequestResult(
        to: Modal.Model.Chain,
        onError: (String?) -> Unit,
        onReject: () -> Unit
    ) {
        when (this) {
            is SentRequestResult.Coinbase -> this.results.firstOrNull()?.let {
                when (it) {
                    is CoinbaseResult.Error -> {
                        onError(it.message)
                        onReject()
                    }

                    is CoinbaseResult.Result -> {
                        viewModelScope.launch {
                            updatedSessionAfterChainSwitch(Session.Coinbase(to.id, accountData.address))
                            logger.log("Successful request: ${it.value}")
                        }
                    }
                }
            }

            is SentRequestResult.WalletConnect -> logger.log("Successful request: ${this.requestId}")
        }
    }

    private fun switchEthChain(
        to: Modal.Model.Chain,
        onSuccess: (SentRequestResult) -> Unit,
        onError: (String?) -> Unit
    ) {
        appKitEngine.request(
            Request(method = EthUtils.walletSwitchEthChain, params = createSwitchChainParams(to)),
            onSuccess
        ) { onError(it.message) }
    }

    private fun addEthChain(
        to: Modal.Model.Chain, onSuccess: (SentRequestResult) -> Unit, onError: (String?) -> Unit
    ) {
        appKitEngine.request(
            Request(method = EthUtils.walletAddEthChain, params = createAddEthChainParams(to)),
            onSuccess
        ) { onError(it.message) }
    }

    fun getSelectedChainOrFirst() = appKitEngine.getSelectedChainOrFirst()

    fun navigateToHelp() {
        sendEventUseCase.send(Props(EventType.TRACK, EventType.Track.CLICK_NETWORK_HELP))
        navigateTo(Route.WHAT_IS_WALLET.path)
    }
}
