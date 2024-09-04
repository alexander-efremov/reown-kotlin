package com.walletconnect.android.internal.common.crypto.codec

import com.walletconnect.android.internal.common.model.EnvelopeType
import com.walletconnect.android.internal.common.model.Participants
import com.reown.foundation.common.model.Topic

interface Codec {
    fun encrypt(topic: Topic, payload: String, envelopeType: EnvelopeType, participants: Participants? = null): ByteArray
    fun decrypt(topic: Topic, cipherText: ByteArray): String
}