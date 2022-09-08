package com.walletconnect.auth.signature.cacao

import com.walletconnect.auth.common.model.Cacao
import com.walletconnect.auth.engine.mapper.toFormattedMessage
import com.walletconnect.auth.engine.mapper.toSignature
import com.walletconnect.auth.signature.SignatureType
import com.walletconnect.auth.signature.eip191.EIP191Verifier

internal object CacaoVerifier {
    fun verify(cacao: Cacao): Boolean = when (cacao.signature.t) {
        SignatureType.EIP191.header -> EIP191Verifier.verify(cacao.signature.toSignature(), cacao.payload.toFormattedMessage(), cacao.payload.address)
        else -> false // todo: Add unsupported types handling
    }
}