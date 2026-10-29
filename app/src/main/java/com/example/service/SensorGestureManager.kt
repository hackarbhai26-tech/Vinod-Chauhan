package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class SensorGestureManager(
    private val context: Context,
    private val onShakeDetected: () -> Unit,
    private val onClapDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTimestamp: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    var isShakeEnabled: Boolean = true
    var shakeSensitivity: Int = 5 // 1 to 10

    var isClapEnabled: Boolean = false
    var clapSensitivity: Int = 5 // 1 to 10

    private val _isListeningForClap = MutableStateFlow(false)
    val isListeningForClap: StateFlow<Boolean> = _isListeningForClap.asStateFlow()

    private val _currentSoundLevel = MutableStateFlow(0f)
    val currentSoundLevel: StateFlow<Float> = _currentSoundLevel.asStateFlow()

    private var clapJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startShakeDetection() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopShakeDetection() {
        sensorManager.unregisterListener(this)
    }

    @SuppressLint("MissingPermission")
    fun startClapDetection() {
        if (clapJob?.isActive == true) return
        _isListeningForClap.value = true

        clapJob = scope.launch {
            val sampleRate = 8000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            if (minBufferSize <= 0) {
                _isListeningForClap.value = false
                return@launch
            }

            var audioRecord: AudioRecord? = null
            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    _isListeningForClap.value = false
                    return@launch
                }

                audioRecord.startRecording()
                val buffer = ShortArray(minBufferSize)
                var lastClapTime = 0L

                while (isActive && isClapEnabled) {
                    val read = audioRecord.read(buffer, 0, minBufferSize)
                    if (read > 0) {
                        var sum = 0.0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                        }
                        val rms = sqrt(sum / read)
                        val normalized = (rms / 32768.0).toFloat().coerceIn(0f, 1f)
                        _currentSoundLevel.value = normalized

                        // Threshold dynamically based on sensitivity (1: high threshold / hard, 10: low threshold / easy)
                        val threshold = 0.85f - (clapSensitivity * 0.05f).coerceIn(0.1f, 0.5f)
                        val now = System.currentTimeMillis()

                        if (normalized > threshold && (now - lastClapTime > 1200)) {
                            lastClapTime = now
                            withContext(Dispatchers.Main) {
                                onClapDetected()
                            }
                        }
                    }
                    delay(50)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    audioRecord?.stop()
                    audioRecord?.release()
                } catch (_: Exception) {}
                _isListeningForClap.value = false
                _currentSoundLevel.value = 0f
            }
        }
    }

    fun stopClapDetection() {
        clapJob?.cancel()
        clapJob = null
        _isListeningForClap.value = false
        _currentSoundLevel.value = 0f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isShakeEnabled || event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ

            lastX = x
            lastY = y
            lastZ = z

            val acceleration = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()
            // Threshold based on sensitivity (1: 22f, 10: 9f)
            val threshold = 22f - (shakeSensitivity * 1.3f)
            val currentTime = System.currentTimeMillis()

            if (acceleration > threshold && (currentTime - lastShakeTimestamp > 1000)) {
                lastShakeTimestamp = currentTime
                onShakeDetected()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun release() {
        stopShakeDetection()
        stopClapDetection()
        scope.cancel()
    }
}
