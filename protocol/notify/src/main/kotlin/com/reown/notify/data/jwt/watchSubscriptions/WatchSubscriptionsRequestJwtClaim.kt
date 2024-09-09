@file:JvmSynthetic

package com.reown.notify.data.jwt.watchSubscriptions

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.reown.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class WatchSubscriptionsRequestJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "ksu") val keyserverUrl: String,
    @Json(name = "app") val appDidWeb: String?,
    @Json(name = "act") override val action: String = ACTION_CLAIM_VALUE,
    @Json(name = "mjv") override val version: String = VERSION,
) : NotifyJwtBase {
    override val requiredActionValue: String = ACTION_CLAIM_VALUE
    override val requiredVersionValue: String = VERSION
}

private const val ACTION_CLAIM_VALUE = "notify_watch_subscriptions"
private const val VERSION = "1"
