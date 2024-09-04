package com.walletconnect.sign.test.utils.hybrid

import com.reown.android.Core
import com.walletconnect.sign.client.Sign
import com.walletconnect.sign.test.utils.TestClient
import com.walletconnect.sign.test.utils.globalOnError
import com.walletconnect.sign.test.utils.proposalNamespaces
import timber.log.Timber

val HybridSignClient = TestClient.Hybrid.signClient

val hybridClientConnect = { pairing: Core.Model.Pairing ->
    val connectParams = Sign.Params.Connect(namespaces = proposalNamespaces, optionalNamespaces = null, properties = null, pairing = pairing)
    HybridSignClient.connect(
        connectParams,
        onSuccess = { url -> Timber.d("HybridDappClient: connect onSuccess, url: $url") },
        onError = ::globalOnError
    )
}