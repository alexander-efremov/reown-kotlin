@file:JvmSynthetic

package com.reown.sign.common.model.vo.clientsync.session.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
internal data class SessionRequestVO(
    @Json(name = "method")
    val method: String,
    @Json(name = "params")
    val params: String,
    @Json(name = "expiryTimestamp")
    val expiryTimestamp: Long? = null
)