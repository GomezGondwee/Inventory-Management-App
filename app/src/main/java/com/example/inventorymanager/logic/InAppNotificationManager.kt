package com.example.inventorymanager.logic

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object InAppNotificationManager {
    private val _notifications = MutableStateFlow<List<FCMNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    private val _newNotificationFlow = MutableSharedFlow<FCMNotification>(extraBufferCapacity = 1)
    val newNotificationFlow = _newNotificationFlow.asSharedFlow()

    fun triggerNotification(title: String?, body: String?) {
        val newNotification = FCMNotification(
            id = UUID.randomUUID().toString(),
            title = title ?: "Alert",
            body = body ?: "",
            timestamp = System.currentTimeMillis()
        )
        
        _notifications.update { currentList ->
            listOf(newNotification) + currentList
        }
        
        _unreadCount.update { it + 1 }
        _newNotificationFlow.tryEmit(newNotification)
    }

    fun clearUnreadCount() {
        _unreadCount.value = 0
    }

    fun reset() {
        _notifications.value = emptyList()
        _unreadCount.value = 0
    }
}

data class FCMNotification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long
)
