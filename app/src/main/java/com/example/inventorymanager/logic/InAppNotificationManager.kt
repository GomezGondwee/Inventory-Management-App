package com.example.inventorymanager.logic

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object InAppNotificationManager {
    private val _notifications = MutableSharedFlow<FCMNotification>(extraBufferCapacity = 1)
    val notifications = _notifications.asSharedFlow()

    fun triggerNotification(title: String?, body: String?) {
        _notifications.tryEmit(FCMNotification(title ?: "Alert", body ?: ""))
    }
}

data class FCMNotification(
    val title: String,
    val body: String
)
