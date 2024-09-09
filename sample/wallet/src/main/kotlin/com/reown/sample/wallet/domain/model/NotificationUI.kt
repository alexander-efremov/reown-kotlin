package com.reown.sample.wallet.domain.model

data class NotificationUI(
    val id: String,
    val topic: String,
    val date: String,
    val sentAt: Long,
    val title: String,
    val body: String,
    val url: String?,
    val icon: String?,
    val isUnread: Boolean,
)