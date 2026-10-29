package com.example.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JarvisNotification(
    val id: String,
    val packageName: String,
    val appTitle: String,
    val title: String,
    val text: String,
    val postTime: Long
)

class JarvisNotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isListenerActive.value = true
        refreshActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isListenerActive.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val item = parseNotification(sbn) ?: return
        val current = _notificationsFlow.value.toMutableList()
        current.removeAll { it.id == item.id }
        current.add(0, item)
        _notificationsFlow.value = current.take(30)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        val key = sbn.key
        _notificationsFlow.value = _notificationsFlow.value.filterNot { it.id == key }
    }

    private fun refreshActiveNotifications() {
        try {
            val sbns = activeNotifications ?: return
            val list = sbns.mapNotNull { parseNotification(it) }
            _notificationsFlow.value = list.take(30)
        } catch (_: Exception) {}
    }

    private fun parseNotification(sbn: StatusBarNotification): JarvisNotification? {
        val extras = sbn.notification.extras ?: return null
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        if (title.isBlank() && text.isBlank()) return null

        val appName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            sbn.packageName
        }

        return JarvisNotification(
            id = sbn.key,
            packageName = sbn.packageName,
            appTitle = appName,
            title = title,
            text = text,
            postTime = sbn.postTime
        )
    }

    companion object {
        private val _isListenerActive = MutableStateFlow(false)
        val isListenerActive: StateFlow<Boolean> = _isListenerActive.asStateFlow()

        private val _notificationsFlow = MutableStateFlow<List<JarvisNotification>>(emptyList())
        val notificationsFlow: StateFlow<List<JarvisNotification>> = _notificationsFlow.asStateFlow()

        fun clearList() {
            _notificationsFlow.value = emptyList()
        }
    }
}
