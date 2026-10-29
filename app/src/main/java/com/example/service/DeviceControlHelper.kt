package com.example.service

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder

data class InstalledApp(
    val appName: String,
    val packageName: String
)

class DeviceControlHelper(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private var cameraId: String? = null

    init {
        try {
            val cameraIds = cameraManager?.cameraIdList
            if (!cameraIds.isNullOrEmpty()) {
                cameraId = cameraIds[0]
            }
        } catch (_: Exception) {}
    }

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!_isFlashlightOn.value)
    }

    fun setFlashlight(enabled: Boolean): Boolean {
        val cid = cameraId ?: return false
        return try {
            cameraManager?.setTorchMode(cid, enabled)
            _isFlashlightOn.value = enabled
            true
        } catch (_: Exception) {
            false
        }
    }

    fun setVolumeLevel(levelPercent: Int) {
        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = ((levelPercent / 100f) * max).toInt().coerceIn(0, max)
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        }
    }

    fun getVolumePercent(): Int {
        val am = audioManager ?: return 50
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return if (max > 0) ((current.toFloat() / max) * 100).toInt() else 50
    }

    fun openSettings(type: String) {
        val action = when (type.lowercase()) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "display" -> Settings.ACTION_DISPLAY_SETTINGS
            "notifications" -> Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            "sound", "volume" -> Settings.ACTION_SOUND_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open $type settings", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppByName(appName: String): Boolean {
        val pm = context.packageManager
        val query = appName.lowercase().trim()

        // Common app aliases
        val targetPkg = when {
            query.contains("youtube") -> "com.google.android.youtube"
            query.contains("chrome") -> "com.android.chrome"
            query.contains("whatsapp") -> "com.whatsapp"
            query.contains("map") -> "com.google.android.apps.maps"
            query.contains("camera") -> "com.google.android.GoogleCamera"
            query.contains("spotify") -> "com.spotify.music"
            query.contains("calculator") -> "com.google.android.calculator"
            query.contains("clock") -> "com.google.android.deskclock"
            else -> null
        }

        if (targetPkg != null) {
            val intent = pm.getLaunchIntentForPackage(targetPkg)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return true
            }
        }

        // Search installed applications
        try {
            val packages = pm.getInstalledApplications(0)
            for (app in packages) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                if (label.contains(query) || query.contains(label)) {
                    val intent = pm.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return true
                    }
                }
            }
        } catch (_: Exception) {}
        return false
    }

    fun makePhoneCall(numberOrContact: String) {
        val cleanNumber = numberOrContact.filter { it.isDigit() || it == '+' }
        val uri = Uri.parse("tel:${if (cleanNumber.isNotBlank()) cleanNumber else "100"}")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppMessage(phone: String = "", message: String) {
        try {
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val url = if (phone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$phone&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMsg"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback generic send
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(Intent.createChooser(fallbackIntent, "Send message via...").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun performWebSearch(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${URLEncoder.encode(query, "UTF-8")}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }
}
