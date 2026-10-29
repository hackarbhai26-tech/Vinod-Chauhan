package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisUiState
import com.example.viewmodel.JarvisViewModel

@Composable
fun PrivacySecurityScreen(
    viewModel: JarvisViewModel,
    uiState: JarvisUiState,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val context = LocalContext.current

    var apiKeyInput by remember { mutableStateOf(uiState.apiKey) }
    var backendUrlInput by remember { mutableStateOf(uiState.backendUrl) }
    var assistantNameInput by remember { mutableStateOf(uiState.assistantName) }
    var wakeWordInput by remember { mutableStateOf(uiState.wakeWord) }

    // Permission check states
    var micGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var cameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(hudTheme.backgroundStart)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Stop / Kill Switch Card
        item {
            HudCard(
                title = "EMERGENCY SUBSYSTEM OVERRIDE",
                statusIndicator = if (uiState.isEmergencyStopped) "SHUTDOWN" else "ARMED"
            ) {
                Text(
                    text = "Instantly cuts off all microphone recognition, audio playback, sensor polling, and automation routines.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isEmergencyStopped) {
                    Button(
                        onClick = { viewModel.resumeAllServices() },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("restore_kill_switch_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Restore")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DISENGAGE KILL SWITCH & RESTORE SYSTEMS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.triggerEmergencyStop() },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("emergency_kill_switch_button")
                    ) {
                        Icon(imageVector = Icons.Default.Dangerous, contentDescription = "Kill Switch")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ENGAGE EMERGENCY KILL SWITCH", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Permissions Dashboard & Security Audit
        item {
            HudCard(title = "PERMISSIONS AUDIT & PRIVACY GUARD") {
                Text(
                    text = "JARVIS operates on a strict least-privilege security model. No private files, contacts, or audio data are transmitted without explicit operator action.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Microphone Permission Row
                PermissionAuditRow(
                    name = "MICROPHONE (AUDIO)",
                    description = "Required for speech recognition and acoustic sound peak detection.",
                    isGranted = micGranted,
                    onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Camera / Torch Permission Row
                PermissionAuditRow(
                    name = "CAMERA (TORCH/FLASHLIGHT)",
                    description = "Used strictly for hardware flashlight emitter control.",
                    isGranted = cameraGranted,
                    onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
        }

        // Assistant Persona & Wake Word
        item {
            HudCard(title = "ASSISTANT PERSONA CONFIGURATION") {
                OutlinedTextField(
                    value = assistantNameInput,
                    onValueChange = {
                        assistantNameInput = it
                        viewModel.setAssistantName(it)
                    },
                    label = { Text("Assistant Call-sign (Default: JARVIS)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("assistant_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = wakeWordInput,
                    onValueChange = {
                        wakeWordInput = it
                        viewModel.setWakeWord(it)
                    },
                    label = { Text("Activation Wake Word (e.g. Hey Jarvis)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("wake_word_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Speech Rate Slider
                Text(
                    text = "SPEECH TEMPO RATE: ${String.format("%.1f", uiState.speechRate)}x",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = hudTheme.primary
                )
                Slider(
                    value = uiState.speechRate,
                    onValueChange = { viewModel.setSpeechRate(it) },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(thumbColor = hudTheme.primary, activeTrackColor = hudTheme.primary),
                    modifier = Modifier.testTag("speech_rate_slider")
                )

                // Speech Pitch Slider
                Text(
                    text = "VOCAL FREQUENCY PITCH: ${String.format("%.1f", uiState.speechPitch)}x",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = hudTheme.primary
                )
                Slider(
                    value = uiState.speechPitch,
                    onValueChange = { viewModel.setSpeechPitch(it) },
                    valueRange = 0.6f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = hudTheme.primary, activeTrackColor = hudTheme.primary),
                    modifier = Modifier.testTag("speech_pitch_slider")
                )
            }
        }

        // Cloud AI API & Endpoint Configuration
        item {
            HudCard(title = "SECURE CLOUD AI INTEGRATION") {
                Text(
                    text = "When an API key is configured, queries are processed via encrypted HTTPS. When omitted, JARVIS relies completely on the built-in local offline neural engine.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("OpenAI / Cloud API Key (Optional)") },
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("api_key_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = backendUrlInput,
                    onValueChange = { backendUrlInput = it },
                    label = { Text("Cloud Backend REST API Endpoint") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("backend_url_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.setApiKey(apiKeyInput.trim())
                        viewModel.setBackendUrl(backendUrlInput.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = hudTheme.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("save_cloud_api_settings_button")
                ) {
                    Text("SAVE CREDENTIALS TO SECURE VAULT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Conversation History Scrubbing
        item {
            HudCard(title = "LOCAL DATA PURGE") {
                Text(
                    text = "Permanently erase all locally stored conversation messages, action history, and session logs from this smartphone.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.clearChatHistory() },
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("purge_history_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Purge", tint = JarvisRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PURGE ALL CONVERSATION MEMORY", color = JarvisRed, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun PermissionAuditRow(
    name: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    val hudTheme = LocalHudTheme.current

    Surface(
        color = hudTheme.backgroundEnd.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, hudTheme.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
            if (isGranted) {
                Surface(
                    color = JarvisGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JarvisGreen)
                ) {
                    Text(
                        text = "GRANTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = JarvisGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onRequest,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("AUTHORIZE", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = hudTheme.primary)
                }
            }
        }
    }
}
