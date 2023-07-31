package com.walletconnect.sample.web3inbox

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.sample.web3inbox.domain.EthAccount
import com.walletconnect.sample.web3inbox.domain.SharedPrefStorage
import com.walletconnect.sample.web3inbox.domain.WCMDelegate
import com.walletconnect.sample.web3inbox.domain.Web3InboxInitializer
import com.walletconnect.wcmodal.client.Modal
import com.walletconnect.wcmodal.client.WalletConnectModal
import timber.log.Timber

class W3ISampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        val serverUri = "wss://relay.walletconnect.com?projectId=${BuildConfig.PROJECT_ID}"
        val appMetaData = Core.Model.AppMetaData(
            name = "Kotlin W3I Sample",
            description = "Kotlin W3I Sample Implementation",
            url = "kotlin.sample.w3i.walletconnect.com",
            icons = listOf("https://gblobscdn.gitbook.com/spaces%2F-LJJeCjcLrr53DcT1Ml7%2Favatar.png?alt=media"),
            redirect = "kotlin-sample-w3i-wc://request"
        )

        CoreClient.initialize(
            relayServerUrl = serverUri,
            connectionType = ConnectionType.AUTOMATIC,
            application = this,
            metaData = appMetaData,
        ) { error -> Timber.e(error.throwable) }

        FirebaseMessaging.getInstance().deleteToken().addOnSuccessListener {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {}
        }

        SharedPrefStorage.getLastLoggedInAccount(this)?.let { account ->
            Timber.d("Last logged in: $account")
            Web3InboxInitializer.init(account, EthAccount.Random(this))
        } ?: run {
            Timber.d("No last logged in account")
        }

        WalletConnectModal.initialize(
            Modal.Params.Init(CoreClient),
            onSuccess = { WalletConnectModal.setDelegate(WCMDelegate) },
            onError = { error -> Timber.e(error.throwable) }
        )
    }
}