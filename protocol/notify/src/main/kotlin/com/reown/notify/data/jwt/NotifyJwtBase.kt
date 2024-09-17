@file:JvmSynthetic

package com.reown.notify.data.jwt

import com.reown.foundation.util.jwt.JwtClaims
import java.util.concurrent.TimeUnit

internal interface NotifyJwtBase : JwtClaims {
    val action: String
    val issuedAt: Long
    val expiration: Long
    val requiredActionValue: String
    val version: String
    val requiredVersionValue: String

    private fun throwIdIssuedAtIsInvalid() {
        val currentTimeSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS) + TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES)
        if (issuedAt > currentTimeSeconds)
            throw IllegalArgumentException("Invalid issuedAt claim was $issuedAt instead of lower than $currentTimeSeconds")
    }

    private fun throwExpirationAtIsInvalid() {
        val currentTimeSeconds = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS) - TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES)
        if (expiration < currentTimeSeconds)
            throw IllegalArgumentException("Invalid expiration claim was $expiration instead of greater than $currentTimeSeconds")
    }

    private fun throwIfActionIsInvalid() {
        if (action != requiredActionValue) throw IllegalArgumentException("Invalid action claim was $action instead of $requiredActionValue")
    }

    private fun throwIfVersionIsInvalid() {
        if (version != requiredVersionValue) throw IllegalArgumentException("Invalid version claim was $version instead of $requiredVersionValue")
    }

    fun throwIfBaseIsInvalid() {
        throwIdIssuedAtIsInvalid()
        throwExpirationAtIsInvalid()
        throwIfActionIsInvalid()
        throwIfVersionIsInvalid()
    }
}