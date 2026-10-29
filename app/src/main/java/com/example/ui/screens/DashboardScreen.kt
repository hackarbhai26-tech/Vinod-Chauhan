package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.JarvisNotification
import com.example.ui.components.CommandTerminal
import com.example.ui.components.HudCard
import com.example.ui.components.JarvisOrb
import com.example.ui.theme.*
import com.example.viewmodel.JarvisUiState
import com.example.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: JarvisViewModel,
    uiState: JarvisUiState,
    onNavigateToConversation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val notifications by viewModel.liveNotifications.collectAsState(emptyList())

    // Live Clock & Date ticker
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateSdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeStr = timeSdf.format(now)
            currentDateStr = dateSdf.format(now).uppercase()
            kotlinx.coroutines.delay(1000)
        }
    }

    val orbSizeDp = when (uiState.orbSize) {
        "Small" -> 130.dp
        "Large" -> 230.dp
        "Giant" -> 270.dp
        else -> 180.dp
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(hudTheme.backgroundStart)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Stop Warning Banner
        if (uiState.isEmergencyStopped) {
            item {
                Surface(
                    color = JarvisRed.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, JarvisRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("emergency_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "!! EMERGENCY KILL SWITCH ENGAGED !!",
                                color = JarvisRed,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Sensors, microphone, and TTS halted.",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { viewModel.resumeAllServices() },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisRed),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("resume_systems_button")
                        ) {
                            Text("RESTORE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Top HUD Header: System Status, Live Clock, Date
        item {
            HudCard(title = "SYSTEM TELEMETRY", statusIndicator = "ONLINE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentTimeStr,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = hudTheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentDateStr,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }

                    // Battery Indicator
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.systemStats.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                contentDescription = "Battery",
                                tint = if (uiState.systemStats.batteryPercent < 20) JarvisRed else hudTheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.systemStats.batteryPercent}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = if (uiState.systemStats.isCharging) "CHARGING" else "BATTERY DISCHARGE",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (uiState.systemStats.isCharging) JarvisGreen else TextMuted
                        )
                    }
                }
            }
        }

        // Animated JARVIS Circular AI Orb Card
        item {
            HudCard(
                title = "CORE NEURAL MATRIX",
                statusIndicator = if (uiState.isListening) "LISTENING" else if (uiState.isSpeaking) "SPEAKING" else "STANDBY"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisOrb(
                        isListening = uiState.isListening,
                        isSpeaking = uiState.isSpeaking,
                        audioRms = uiState.audioRms,
                        sizeDp = orbSizeDp,
                        onClick = {
                            if (uiState.isListening) {
                                viewModel.stopVoiceListening()
                            } else {
                                viewModel.startVoiceListening()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (uiState.isListening) "LISTENING TO VOCAL TELEMETRY..."
                        else if (uiState.isSpeaking) "SYNTHESIZING VOCAL OUTPUT..."
                        else "TAP ORB OR SAY '${uiState.wakeWord.uppercase()}'",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = if (uiState.isListening) Color.White else hudTheme.primary
                    )

                    if (uiState.isSpeaking) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.stopSpeaking() },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp).testTag("silence_tts_button")
                        ) {
                            Text("SILENCE VOCALS", fontSize = 10.sp, color = JarvisAmber, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Hardware Diagnostics: RAM & Storage Usage
        item {
            HudCard(title = "HARDWARE RESOURCES", statusIndicator = "ACTIVE") {
                // RAM Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RAM USAGE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = "${uiState.systemStats.ramUsedMb} MB / ${uiState.systemStats.ramTotalMb} MB (${uiState.systemStats.ramPercent}%)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (uiState.systemStats.ramPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = hudTheme.primary,
                        trackColor = hudTheme.surface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Storage Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "INTERNAL STORAGE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = "${uiState.systemStats.storageUsedGb} GB / ${uiState.systemStats.storageTotalGb} GB (${uiState.systemStats.storagePercent}%)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (uiState.systemStats.storagePercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = hudTheme.secondary,
                        trackColor = hudTheme.surface
                    )
                }
            }
        }

        // Weather & News Announcement Dispatch
        item {
            HudCard(title = "ENVIRONMENT & NEWS FEED") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(hudTheme.surface, CircleShape)
                            .border(1.dp, hudTheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Weather",
                            tint = JarvisAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "24°C • CLEAR SKY • WIND 9 KM/H",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = "Global news: Quantum AI acceleration & space orbital achievements announced.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.submitUserQuery("Announce weather and news summary")
                        },
                        modifier = Modifier.testTag("speak_weather_news_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = hudTheme.primary
                        )
                    }
                }
            }
        }

        // Quick Tactical Actions Bar
        item {
            HudCard(title = "TACTICAL CONTROLS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(
                        icon = Icons.Default.FlashlightOn,
                        label = if (uiState.isFlashlightOn) "TORCH ON" else "TORCH OFF",
                        isActive = uiState.isFlashlightOn,
                        onClick = { viewModel.toggleFlashlight() }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Chat,
                        label = "DISPATCH",
                        onClick = onNavigateToConversation
                    )
                    QuickActionButton(
                        icon = Icons.Default.Wifi,
                        label = "WI-FI",
                        onClick = { viewModel.openSettings("wifi") }
                    )
                    QuickActionButton(
                        icon = Icons.Default.Bluetooth,
                        label = "BLUETOOTH",
                        onClick = { viewModel.openSettings("bluetooth") }
                    )
                }
            }
        }

        // Live Command Terminal Console
        item {
            HudCard(title = "COMMAND TERMINAL LOG", statusIndicator = "${uiState.terminalLogs.size} EVENTS") {
                CommandTerminal(logs = uiState.terminalLogs)
            }
        }

        // Notifications Feed (if available)
        if (notifications.isNotEmpty()) {
            item {
                HudCard(title = "NOTIFICATION FEED", statusIndicator = "${notifications.size} RECENT") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notifications.take(3).forEach { notif ->
                            NotificationItemRow(notif = notif)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val hudTheme = LocalHudTheme.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag("quick_action_${label.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isActive) hudTheme.primary.copy(alpha = 0.25f) else hudTheme.surface,
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    if (isActive) hudTheme.primary else hudTheme.border,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) hudTheme.primary else TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = if (isActive) hudTheme.primary else TextSecondary
        )
    }
}

@Composable
fun NotificationItemRow(notif: JarvisNotification) {
    val hudTheme = LocalHudTheme.current
    Surface(
        color = hudTheme.backgroundEnd.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, hudTheme.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(hudTheme.primary, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${notif.appTitle}: ${notif.title}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                if (notif.text.isNotBlank()) {
                    Text(
                        text = notif.text,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
