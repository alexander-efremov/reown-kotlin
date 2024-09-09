package com.reown.android.internal.common.model.params

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.reown.android.internal.common.model.type.ClientParams

interface ChatNotifyResponseAuthParams {
    @JsonClass(generateAdapter = true)
    data class ResponseAuth(
        @Json(name = "responseAuth")
        val responseAuth: String,
    ) : ClientParams

    @JsonClass(generateAdapter = true)
    data class Auth(
        @Json(name = "auth")
        val auth: String,
    ) : ClientParams
}