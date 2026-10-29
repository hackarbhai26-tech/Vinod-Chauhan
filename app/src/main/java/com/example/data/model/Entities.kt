package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageSender {
    USER,
    JARVIS,
    SYSTEM
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpoken: Boolean = false
)

enum class ReminderType {
    HEALTH,
    HYDRATION,
    MEDICATION,
    CUSTOM
}

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timeFormatted: String,
    val type: ReminderType,
    val isEnabled: Boolean = true
)
