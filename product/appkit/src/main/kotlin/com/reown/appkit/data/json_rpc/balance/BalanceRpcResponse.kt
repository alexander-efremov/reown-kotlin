package com.reown.appkit.data.json_rpc.balance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class BalanceRpcResponse(
    @Json(name = "id")
    val id: Long,
    @Json(name = "jsonrpc")
    val jsonrpc: String = "2.0",
    @Json(name = "result")
    val result: String?,
    @Json(name = "error")
    val error: Error?,
)

@JsonClass(generateAdapter = true)
internal data class Error(
    @Json(name = "code")
    val code: Int,
    @Json(name = "message")
    val message: String,
)