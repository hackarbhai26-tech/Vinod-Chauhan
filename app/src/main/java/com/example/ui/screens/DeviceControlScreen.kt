package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReminderItem
import com.example.data.model.ReminderType
import com.example.service.JarvisAccessibilityService
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisUiState
import com.example.viewmodel.JarvisViewModel

@Composable
fun DeviceControlScreen(
    viewModel: JarvisViewModel,
    uiState: JarvisUiState,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val reminders by viewModel.reminders.collectAsState(emptyList())

    var showAddReminderDialog by remember { mutableStateOf(false) }
    var newReminderTitle by remember { mutableStateOf("") }
    var newReminderType by remember { mutableStateOf(ReminderType.HYDRATION) }
    var newReminderTime by remember { mutableStateOf("09:00") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(hudTheme.backgroundStart)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hardware Toggles
        item {
            HudCard(title = "CORE HARDWARE INTERFACES") {
                // Flashlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = "Flashlight",
                            tint = if (uiState.isFlashlightOn) JarvisAmber else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TACTICAL TORCH / FLASHLIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Text(
                                text = if (uiState.isFlashlightOn) "BEAM EMITTER ACTIVE" else "STANDBY",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (uiState.isFlashlightOn) JarvisGreen else TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = uiState.isFlashlightOn,
                        onCheckedChange = { viewModel.toggleFlashlight() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = hudTheme.primary,
                            checkedTrackColor = hudTheme.primary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("flashlight_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Media Volume Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MEDIA AUDIO LEVEL",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                        Text(
                            text = "${uiState.volumePercent}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = hudTheme.primary
                        )
                    }
                    Slider(
                        value = uiState.volumePercent.toFloat(),
                        onValueChange = { viewModel.setVolume(it.toInt()) },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = hudTheme.primary,
                            activeTrackColor = hudTheme.primary,
                            inactiveTrackColor = hudTheme.surface
                        ),
                        modifier = Modifier.testTag("volume_slider")
                    )
                }
            }
        }

        // Accessibility Service Subsystem
        item {
            val isA11yActive = JarvisAccessibilityService.isServiceActive
            HudCard(
                title = "ACCESSIBILITY AUTOMATION SERVICE",
                statusIndicator = if (isA11yActive) "ENABLED" else "DISABLED"
            ) {
                Text(
                    text = "Allows JARVIS to execute user-directed navigation (Back, Home, Recents, Scrolling) through official Android Accessibility APIs.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isA11yActive) "STATUS: OPERATIONAL" else "STATUS: REQUIRES USER PERMISSION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isA11yActive) JarvisGreen else JarvisAmber
                    )

                    Button(
                        onClick = { viewModel.openSettings("accessibility") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isA11yActive) hudTheme.surface else hudTheme.primary,
                            contentColor = if (isA11yActive) hudTheme.primary else Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("open_accessibility_settings_button")
                    ) {
                        Text("CONFIGURE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // System Settings Shortcuts
        item {
            HudCard(title = "SYSTEM SETTINGS PORTALS") {
                val settings = listOf(
                    Triple("WI-FI", Icons.Default.Wifi, "wifi"),
                    Triple("BLUETOOTH", Icons.Default.Bluetooth, "bluetooth"),
                    Triple("DISPLAY", Icons.Default.BrightnessMedium, "display"),
                    Triple("SOUND", Icons.Default.VolumeUp, "sound"),
                    Triple("NOTIFICATIONS", Icons.Default.Notifications, "notifications")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    settings.forEach { (label, icon, key) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.openSettings(key) }
                                .padding(4.dp)
                                .testTag("shortcut_$key")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(hudTheme.surface, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = hudTheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Health, Hydration & Medication Reminders
        item {
            HudCard(
                title = "REMINDERS & DIRECTIVES",
                statusIndicator = "${reminders.size} ACTIVE"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEALTH & PRODUCTIVITY SCHEDULE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = { showAddReminderDialog = true },
                        modifier = Modifier.testTag("add_reminder_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Reminder",
                            tint = hudTheme.primary
                        )
                    }
                }

                if (reminders.isEmpty()) {
                    Text(
                        text = "No active reminders. Tap '+' or say 'Remind me to drink water'.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reminders.forEach { rem ->
                            ReminderItemRow(
                                reminder = rem,
                                onToggle = { viewModel.toggleReminder(rem) },
                                onDelete = { viewModel.deleteReminder(rem) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddReminderDialog) {
        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            containerColor = hudTheme.surface,
            title = {
                Text(
                    text = "NEW SCHEDULED DIRECTIVE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = hudTheme.primary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newReminderTitle,
                        onValueChange = { newReminderTitle = it },
                        label = { Text("Directive Title (e.g. Drink 500ml Water)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_reminder_title_input")
                    )
                    OutlinedTextField(
                        value = newReminderTime,
                        onValueChange = { newReminderTime = it },
                        label = { Text("Scheduled Time (HH:mm)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_reminder_time_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReminderType.values().forEach { type ->
                            FilterChip(
                                selected = newReminderType == type,
                                onClick = { newReminderType = type },
                                label = { Text(type.name, fontSize = 9.sp) },
                                modifier = Modifier.testTag("rem_type_${type.name}")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newReminderTitle.isNotBlank()) {
                            viewModel.addReminder(newReminderTitle, newReminderType, newReminderTime)
                            newReminderTitle = ""
                            showAddReminderDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = hudTheme.primary, contentColor = Color.Black),
                    modifier = Modifier.testTag("save_reminder_button")
                ) {
                    Text("SAVE", fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ReminderItemRow(
    reminder: ReminderItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val hudTheme = LocalHudTheme.current

    Surface(
        color = hudTheme.backgroundEnd.copy(alpha = 0.6f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, hudTheme.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = hudTheme.primary,
                        checkmarkColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = reminder.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (reminder.isEnabled) TextPrimary else TextMuted
                    )
                    Text(
                        text = "${reminder.type.name} • ${reminder.timeFormatted}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = hudTheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Reminder",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
