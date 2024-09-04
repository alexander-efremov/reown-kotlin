package com.walletconnect.web3.modal.client.models.request

import com.reown.sign.client.Sign
import com.walletconnect.web3.modal.engine.coinbase.CoinbaseResult

sealed class SentRequestResult {
    abstract val method: String
    abstract val params: String
    abstract val chainId: String

    data class WalletConnect(
        override val method: String,
        override val params: String,
        override val chainId: String,
        val requestId: Long,
        val sessionTopic: String
    ) : SentRequestResult()

    data class Coinbase(
        override val method: String,
        override val params: String,
        override val chainId: String,
        val results: List<CoinbaseResult>
    ) : SentRequestResult()
}

internal fun Sign.Model.SentRequest.toSentRequest() = SentRequestResult.WalletConnect(method, params, chainId, requestId, sessionTopic)
