package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_user_preferences", Context.MODE_PRIVATE)

    var hudThemeIndex: Int
        get() = prefs.getInt(KEY_HUD_THEME, 0)
        set(value) = prefs.edit().putInt(KEY_HUD_THEME, value).apply()

    var orbSize: String
        get() = prefs.getString(KEY_ORB_SIZE, "Medium") ?: "Medium"
        set(value) = prefs.edit().putString(KEY_ORB_SIZE, value).apply()

    var orbPosition: String
        get() = prefs.getString(KEY_ORB_POSITION, "Center") ?: "Center"
        set(value) = prefs.edit().putString(KEY_ORB_POSITION, value).apply()

    var assistantName: String
        get() = prefs.getString(KEY_ASSISTANT_NAME, "JARVIS") ?: "JARVIS"
        set(value) = prefs.edit().putString(KEY_ASSISTANT_NAME, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var speechRate: Float
        get() = prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_RATE, value).apply()

    var speechPitch: Float
        get() = prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_SPEECH_PITCH, value).apply()

    var wakeWord: String
        get() = prefs.getString(KEY_WAKE_WORD, "Hey Jarvis") ?: "Hey Jarvis"
        set(value) = prefs.edit().putString(KEY_WAKE_WORD, value).apply()

    var shakeAction: String
        get() = prefs.getString(KEY_SHAKE_ACTION, "Flashlight") ?: "Flashlight"
        set(value) = prefs.edit().putString(KEY_SHAKE_ACTION, value).apply()

    var isShakeEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHAKE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SHAKE_ENABLED, value).apply()

    var shakeSensitivity: Int
        get() = prefs.getInt(KEY_SHAKE_SENSITIVITY, 5)
        set(value) = prefs.edit().putInt(KEY_SHAKE_SENSITIVITY, value).apply()

    var clapAction: String
        get() = prefs.getString(KEY_CLAP_ACTION, "Voice Command") ?: "Voice Command"
        set(value) = prefs.edit().putString(KEY_CLAP_ACTION, value).apply()

    var isClapEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLAP_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CLAP_ENABLED, value).apply()

    var clapSensitivity: Int
        get() = prefs.getInt(KEY_CLAP_SENSITIVITY, 5)
        set(value) = prefs.edit().putInt(KEY_CLAP_SENSITIVITY, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var backendUrl: String
        get() = prefs.getString(KEY_BACKEND_URL, "https://api.openai.com/v1") ?: "https://api.openai.com/v1"
        set(value) = prefs.edit().putString(KEY_BACKEND_URL, value).apply()

    var isEmergencyStopped: Boolean
        get() = prefs.getBoolean(KEY_EMERGENCY_STOP, false)
        set(value) = prefs.edit().putBoolean(KEY_EMERGENCY_STOP, value).apply()

    var notificationFilter: String
        get() = prefs.getString(KEY_NOTIFICATION_FILTER, "ALL") ?: "ALL"
        set(value) = prefs.edit().putString(KEY_NOTIFICATION_FILTER, value).apply()

    companion object {
        private const val KEY_HUD_THEME = "hud_theme_idx"
        private const val KEY_ORB_SIZE = "orb_size"
        private const val KEY_ORB_POSITION = "orb_pos"
        private const val KEY_ASSISTANT_NAME = "asst_name"
        private const val KEY_LANGUAGE = "lang_code"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_SPEECH_PITCH = "speech_pitch"
        private const val KEY_WAKE_WORD = "wake_word"
        private const val KEY_SHAKE_ACTION = "shake_act"
        private const val KEY_SHAKE_ENABLED = "shake_en"
        private const val KEY_SHAKE_SENSITIVITY = "shake_sens"
        private const val KEY_CLAP_ACTION = "clap_act"
        private const val KEY_CLAP_ENABLED = "clap_en"
        private const val KEY_CLAP_SENSITIVITY = "clap_sens"
        private const val KEY_API_KEY = "openai_key"
        private const val KEY_BACKEND_URL = "backend_url"
        private const val KEY_EMERGENCY_STOP = "emergency_stop"
        private const val KEY_NOTIFICATION_FILTER = "notif_filter"
    }
}
