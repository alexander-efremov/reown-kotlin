package com.walletconnect.android.internal.common.crypto.kmr

import com.walletconnect.android.internal.common.model.MissingKeyException
import com.walletconnect.android.internal.common.model.SymmetricKey
import com.reown.foundation.common.model.Key
import com.reown.foundation.common.model.PrivateKey
import com.reown.foundation.common.model.PublicKey
import com.reown.foundation.common.model.Topic

interface KeyManagementRepository {
    fun setKey(key: Key, tag: String)
    @Throws(MissingKeyException::class)
    fun removeKeys(tag: String)

    fun getPublicKey(tag: String): PublicKey
    fun getSymmetricKey(tag: String): SymmetricKey
    fun getKeyPair(key: PublicKey): Pair<PublicKey, PrivateKey>
    fun setKeyPair(publicKey: PublicKey, privateKey: PrivateKey)
    fun generateAndStoreEd25519KeyPair(): PublicKey

    fun deriveAndStoreEd25519KeyPair(privateKey: PrivateKey): PublicKey

    fun generateAndStoreX25519KeyPair(): PublicKey
    fun setKeyAgreement(topic: Topic, self: PublicKey, peer: PublicKey)
    fun getSelfPublicFromKeyAgreement(topic: Topic): PublicKey

    fun generateAndStoreSymmetricKey(topic: Topic): SymmetricKey
    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey

    fun getTopicFromKey(key: Key): Topic
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): Topic
}