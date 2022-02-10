package com.walletconnect.walletconnectv2.core.model.vo.clientsync.session.common

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SessionStateVO(val accounts: List<String>)