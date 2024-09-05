@file:JvmSynthetic

package com.reown.notify.data.jwt.subscriptionsChanged

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.reown.notify.common.model.ServerSubscription
import com.reown.notify.data.jwt.NotifyJwtBase

@JsonClass(generateAdapter = true)
internal data class SubscriptionsChangedRequestJwtClaim(
    @Json(name = "iss") override val issuer: String,
    @Json(name = "sub") val subject: String,
    @Json(name = "aud") val audience: String,
    @Json(name = "iat") override val issuedAt: Long,
    @Json(name = "exp") override val expiration: Long,
    @Json(name = "sbs") val subscriptions: List<ServerSubscription>,
    @Json(name = "act") override val action: String = ACTION_CLAIM_VALUE,
    @Json(name = "mjv") override val version: String = VERSION,
) : NotifyJwtBase {
    override val requiredActionValue: String = ACTION_CLAIM_VALUE
    override val requiredVersionValue: String = VERSION
}

private const val ACTION_CLAIM_VALUE = "notify_subscriptions_changed"
private const val VERSION = "1"