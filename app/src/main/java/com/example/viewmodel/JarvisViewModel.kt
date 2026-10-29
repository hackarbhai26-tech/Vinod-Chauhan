package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JarvisDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.ReminderItem
import com.example.data.model.ReminderType
import com.example.service.*
import com.example.ui.components.TerminalLog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class JarvisUiState(
    val hudThemeIndex: Int = 0,
    val orbSize: String = "Medium",
    val orbPosition: String = "Center",
    val assistantName: String = "JARVIS",
    val language: String = "en",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val wakeWord: String = "Hey Jarvis",
    val isShakeEnabled: Boolean = true,
    val shakeSensitivity: Int = 5,
    val shakeAction: String = "Flashlight",
    val isClapEnabled: Boolean = false,
    val clapSensitivity: Int = 5,
    val clapAction: String = "Voice Command",
    val apiKey: String = "",
    val backendUrl: String = "https://api.openai.com/v1",
    val isEmergencyStopped: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val audioRms: Float = 0f,
    val isClapListening: Boolean = false,
    val currentSoundLevel: Float = 0f,
    val volumePercent: Int = 50,
    val systemStats: SystemStats = SystemStats(),
    val terminalLogs: List<TerminalLog> = emptyList(),
    val pendingActionDialog: PendingAction? = null
)

sealed class PendingAction {
    data class CallAction(val contactOrNumber: String) : PendingAction()
    data class WhatsAppAction(val message: String) : PendingAction()
    data class DeleteFileAction(val file: File, val onConfirmed: () -> Unit) : PendingAction()
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val reminderDao = db.reminderDao()
    private val prefs = PreferencesManager(application)

    private val deviceHelper = DeviceControlHelper(application)
    private val statsMonitor = SystemStatsMonitor(application)
    private val aiEngine = AiAssistantEngine()
    val fileManager = FileManagerHelper(application)

    private lateinit var voiceManager: VoiceSpeechManager
    private lateinit var gestureManager: SensorGestureManager

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    val chatMessages = chatDao.getAllMessagesFlow()
    val reminders = reminderDao.getAllRemindersFlow()
    val liveNotifications = JarvisNotificationService.notificationsFlow

    init {
        initServices(application)
        loadInitialPreferences()
        observeManagers()
        logTerminal("JARVIS AI protocols initialized. Status: READY", isSystem = true)
    }

    private fun initServices(app: Application) {
        voiceManager = VoiceSpeechManager(app) { speechText ->
            handleSpeechInput(speechText)
        }

        gestureManager = SensorGestureManager(
            context = app,
            onShakeDetected = { handleShakeGesture() },
            onClapDetected = { handleClapGesture() }
        )

        gestureManager.isShakeEnabled = prefs.isShakeEnabled
        gestureManager.shakeSensitivity = prefs.shakeSensitivity
        gestureManager.isClapEnabled = prefs.isClapEnabled
        gestureManager.clapSensitivity = prefs.clapSensitivity

        if (prefs.isShakeEnabled && !prefs.isEmergencyStopped) {
            gestureManager.startShakeDetection()
        }
        if (prefs.isClapEnabled && !prefs.isEmergencyStopped) {
            gestureManager.startClapDetection()
        }
    }

    private fun loadInitialPreferences() {
        _uiState.update {
            it.copy(
                hudThemeIndex = prefs.hudThemeIndex,
                orbSize = prefs.orbSize,
                orbPosition = prefs.orbPosition,
                assistantName = prefs.assistantName,
                language = prefs.language,
                speechRate = prefs.speechRate,
                speechPitch = prefs.speechPitch,
                wakeWord = prefs.wakeWord,
                isShakeEnabled = prefs.isShakeEnabled,
                shakeSensitivity = prefs.shakeSensitivity,
                shakeAction = prefs.shakeAction,
                isClapEnabled = prefs.isClapEnabled,
                clapSensitivity = prefs.clapSensitivity,
                clapAction = prefs.clapAction,
                apiKey = prefs.apiKey,
                backendUrl = prefs.backendUrl,
                isEmergencyStopped = prefs.isEmergencyStopped,
                volumePercent = deviceHelper.getVolumePercent()
            )
        }
    }

    private fun observeManagers() {
        viewModelScope.launch {
            statsMonitor.stats.collect { stats ->
                _uiState.update { it.copy(systemStats = stats) }
            }
        }
        viewModelScope.launch {
            deviceHelper.isFlashlightOn.collect { on ->
                _uiState.update { it.copy(isFlashlightOn = on) }
            }
        }
        viewModelScope.launch {
            voiceManager.isListening.collect { listening ->
                _uiState.update { it.copy(isListening = listening) }
            }
        }
        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
        viewModelScope.launch {
            voiceManager.audioRms.collect { rms ->
                _uiState.update { it.copy(audioRms = rms) }
            }
        }
        viewModelScope.launch {
            gestureManager.isListeningForClap.collect { listening ->
                _uiState.update { it.copy(isClapListening = listening) }
            }
        }
        viewModelScope.launch {
            gestureManager.currentSoundLevel.collect { level ->
                _uiState.update { it.copy(currentSoundLevel = level) }
            }
        }
    }

    fun submitUserQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || _uiState.value.isEmergencyStopped) return

        logTerminal("USER: $trimmed")

        viewModelScope.launch {
            // Save user message to database
            chatDao.insertMessage(ChatMessage(sender = MessageSender.USER, text = trimmed))

            // Process via AI engine
            val current = _uiState.value
            val reply = aiEngine.processQuery(
                query = trimmed,
                assistantName = current.assistantName,
                language = current.language,
                apiKey = current.apiKey,
                backendUrl = current.backendUrl
            )

            // Save response to database
            chatDao.insertMessage(ChatMessage(sender = MessageSender.JARVIS, text = reply.responseText))
            logTerminal("${current.assistantName}: ${reply.responseText}", isSystem = true)

            // Speak response if not emergency stopped
            if (!_uiState.value.isEmergencyStopped) {
                voiceManager.speak(
                    text = reply.responseText,
                    languageCode = current.language,
                    pitch = current.speechPitch,
                    rate = current.speechRate
                )
            }

            // Handle execution intent
            executeActionIntent(reply.action)
        }
    }

    private fun handleSpeechInput(recognizedText: String) {
        val wake = _uiState.value.wakeWord.lowercase()
        val textLower = recognizedText.lowercase()

        // Check if wake-word filtered or direct speech
        val processed = if (textLower.startsWith(wake)) {
            recognizedText.substring(wake.length).trim()
        } else {
            recognizedText
        }
        submitUserQuery(if (processed.isNotBlank()) processed else recognizedText)
    }

    private fun executeActionIntent(intent: ActionIntent) {
        when (intent) {
            is ActionIntent.Flashlight -> {
                deviceHelper.setFlashlight(intent.enable)
                logTerminal("Action: Flashlight set to ${intent.enable}", isSystem = true)
            }
            is ActionIntent.OpenApp -> {
                val opened = deviceHelper.openAppByName(intent.appName)
                logTerminal("Action: Open app '${intent.appName}' result: $opened", isSystem = true)
            }
            is ActionIntent.MakeCall -> {
                // Request confirmation before consequential call
                _uiState.update { it.copy(pendingActionDialog = PendingAction.CallAction(intent.query)) }
            }
            is ActionIntent.SendWhatsApp -> {
                // Request confirmation before consequential message
                _uiState.update { it.copy(pendingActionDialog = PendingAction.WhatsAppAction(intent.message)) }
            }
            is ActionIntent.WebSearch -> {
                deviceHelper.performWebSearch(intent.query)
                logTerminal("Action: Web search for '${intent.query}'", isSystem = true)
            }
            is ActionIntent.OpenSettings -> {
                deviceHelper.openSettings(intent.type)
                logTerminal("Action: Open settings '${intent.type}'", isSystem = true)
            }
            is ActionIntent.AddReminder -> {
                val remType = when (intent.type.uppercase()) {
                    "HYDRATION" -> ReminderType.HYDRATION
                    "MEDICATION" -> ReminderType.MEDICATION
                    else -> ReminderType.HEALTH
                }
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(System.currentTimeMillis() + 3600000))
                addReminder(intent.title, remType, timeStr)
            }
            ActionIntent.None -> {}
        }
    }

    fun confirmPendingAction() {
        val action = _uiState.value.pendingActionDialog ?: return
        when (action) {
            is PendingAction.CallAction -> {
                deviceHelper.makePhoneCall(action.contactOrNumber)
                logTerminal("Confirmed: Calling ${action.contactOrNumber}", isSystem = true)
            }
            is PendingAction.WhatsAppAction -> {
                deviceHelper.sendWhatsAppMessage("", action.message)
                logTerminal("Confirmed: Dispatching WhatsApp message", isSystem = true)
            }
            is PendingAction.DeleteFileAction -> {
                action.onConfirmed()
                logTerminal("Confirmed: File deleted safely", isSystem = true)
            }
        }
        _uiState.update { it.copy(pendingActionDialog = null) }
    }

    fun dismissPendingAction() {
        logTerminal("Action aborted by operator.", isSystem = true)
        _uiState.update { it.copy(pendingActionDialog = null) }
    }

    private fun handleShakeGesture() {
        if (_uiState.value.isEmergencyStopped) return
        logTerminal("Sensor: Shake gesture triggered (${_uiState.value.shakeAction})", isSystem = true)
        when (_uiState.value.shakeAction) {
            "Flashlight" -> toggleFlashlight()
            "Voice Command" -> startVoiceListening()
            "Mute" -> stopSpeaking()
            "Emergency Alarm" -> triggerEmergencyStop()
            else -> toggleFlashlight()
        }
    }

    private fun handleClapGesture() {
        if (_uiState.value.isEmergencyStopped) return
        logTerminal("Sensor: Sound peak / clap triggered (${_uiState.value.clapAction})", isSystem = true)
        when (_uiState.value.clapAction) {
            "Voice Command" -> startVoiceListening()
            "Flashlight" -> toggleFlashlight()
            else -> startVoiceListening()
        }
    }

    fun startVoiceListening() {
        if (_uiState.value.isEmergencyStopped) return
        voiceManager.startListening(_uiState.value.language)
        logTerminal("Mic active: Listening for voice input...", isSystem = true)
    }

    fun stopVoiceListening() {
        voiceManager.stopListening()
        logTerminal("Mic deactivated.", isSystem = true)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
        logTerminal("Voice synthesis silenced.", isSystem = true)
    }

    fun toggleFlashlight() {
        val state = deviceHelper.toggleFlashlight()
        logTerminal("Flashlight toggled. Active: $state", isSystem = true)
    }

    fun setVolume(percent: Int) {
        deviceHelper.setVolumeLevel(percent)
        _uiState.update { it.copy(volumePercent = percent) }
    }

    fun openSettings(type: String) {
        deviceHelper.openSettings(type)
    }

    fun triggerEmergencyStop() {
        prefs.isEmergencyStopped = true
        _uiState.update { it.copy(isEmergencyStopped = true) }
        voiceManager.stopListening()
        voiceManager.stopSpeaking()
        gestureManager.stopShakeDetection()
        gestureManager.stopClapDetection()
        deviceHelper.setFlashlight(false)
        logTerminal("!! EMERGENCY KILL SWITCH ENGAGED !! All subsystems offline.", isSystem = true)
    }

    fun resumeAllServices() {
        prefs.isEmergencyStopped = false
        _uiState.update { it.copy(isEmergencyStopped = false) }
        if (prefs.isShakeEnabled) gestureManager.startShakeDetection()
        if (prefs.isClapEnabled) gestureManager.startClapDetection()
        logTerminal("Systems restored. Ready for operations.", isSystem = true)
    }

    fun setHudTheme(index: Int) {
        prefs.hudThemeIndex = index
        _uiState.update { it.copy(hudThemeIndex = index) }
        logTerminal("HUD Theme switched to ID: $index", isSystem = true)
    }

    fun setOrbSize(size: String) {
        prefs.orbSize = size
        _uiState.update { it.copy(orbSize = size) }
    }

    fun setOrbPosition(pos: String) {
        prefs.orbPosition = pos
        _uiState.update { it.copy(orbPosition = pos) }
    }

    fun setAssistantName(name: String) {
        prefs.assistantName = name
        _uiState.update { it.copy(assistantName = name) }
    }

    fun setLanguage(lang: String) {
        prefs.language = lang
        _uiState.update { it.copy(language = lang) }
    }

    fun setSpeechRate(rate: Float) {
        prefs.speechRate = rate
        _uiState.update { it.copy(speechRate = rate) }
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.speechPitch = pitch
        _uiState.update { it.copy(speechPitch = pitch) }
    }

    fun setWakeWord(word: String) {
        prefs.wakeWord = word
        _uiState.update { it.copy(wakeWord = word) }
    }

    fun setShakeEnabled(enabled: Boolean) {
        prefs.isShakeEnabled = enabled
        gestureManager.isShakeEnabled = enabled
        _uiState.update { it.copy(isShakeEnabled = enabled) }
        if (enabled && !_uiState.value.isEmergencyStopped) gestureManager.startShakeDetection()
        else gestureManager.stopShakeDetection()
    }

    fun setShakeSensitivity(sens: Int) {
        prefs.shakeSensitivity = sens
        gestureManager.shakeSensitivity = sens
        _uiState.update { it.copy(shakeSensitivity = sens) }
    }

    fun setShakeAction(action: String) {
        prefs.shakeAction = action
        _uiState.update { it.copy(shakeAction = action) }
    }

    fun setClapEnabled(enabled: Boolean) {
        prefs.isClapEnabled = enabled
        gestureManager.isClapEnabled = enabled
        _uiState.update { it.copy(isClapEnabled = enabled) }
        if (enabled && !_uiState.value.isEmergencyStopped) gestureManager.startClapDetection()
        else gestureManager.stopClapDetection()
    }

    fun setClapSensitivity(sens: Int) {
        prefs.clapSensitivity = sens
        gestureManager.clapSensitivity = sens
        _uiState.update { it.copy(clapSensitivity = sens) }
    }

    fun setClapAction(action: String) {
        prefs.clapAction = action
        _uiState.update { it.copy(clapAction = action) }
    }

    fun setApiKey(key: String) {
        prefs.apiKey = key
        _uiState.update { it.copy(apiKey = key) }
        logTerminal("AI Cloud API key saved securely.", isSystem = true)
    }

    fun setBackendUrl(url: String) {
        prefs.backendUrl = url
        _uiState.update { it.copy(backendUrl = url) }
    }

    fun addReminder(title: String, type: ReminderType, timeStr: String) {
        viewModelScope.launch {
            val item = ReminderItem(
                title = title,
                timeFormatted = timeStr,
                type = type,
                isEnabled = true
            )
            reminderDao.insertReminder(item)
            logTerminal("Reminder logged: $title at $timeStr", isSystem = true)
        }
    }

    fun toggleReminder(item: ReminderItem) {
        viewModelScope.launch {
            reminderDao.updateReminder(item.copy(isEnabled = !item.isEnabled))
        }
    }

    fun deleteReminder(item: ReminderItem) {
        viewModelScope.launch {
            reminderDao.deleteReminder(item)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
            logTerminal("Local conversation history purged.", isSystem = true)
        }
    }

    fun requestFileDeletion(file: File, onConfirmed: () -> Unit) {
        _uiState.update {
            it.copy(
                pendingActionDialog = PendingAction.DeleteFileAction(file, onConfirmed)
            )
        }
    }

    fun logTerminal(msg: String, isSystem: Boolean = false) {
        val newLog = TerminalLog(text = msg, isSystem = isSystem)
        val updated = (_uiState.value.terminalLogs + newLog).takeLast(60)
        _uiState.update { it.copy(terminalLogs = updated) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
        gestureManager.release()
        statsMonitor.release()
    }
}
