package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisUiState
import com.example.viewmodel.JarvisViewModel

@Composable
fun LabGesturesScreen(
    viewModel: JarvisViewModel,
    uiState: JarvisUiState,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current

    val orbSizes = listOf("Small", "Medium", "Large", "Giant")
    val orbPositions = listOf("Top", "Center", "Floating")
    val gestureActions = listOf("Flashlight", "Voice Command", "Mute", "Emergency Alarm")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(hudTheme.backgroundStart)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: 14 Distinct HUD Layouts & Themes
        item {
            HudCard(title = "HUD THEMES (14 STYLES)", statusIndicator = "THEME: ${HudThemes[uiState.hudThemeIndex].name}") {
                Text(
                    text = "Select from 14 futuristic HUD visual styles with tailored neon accents, contrast palettes, and holographic glow.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(HudThemes) { theme ->
                        val isSelected = theme.id == uiState.hudThemeIndex
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) theme.primary else theme.border
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .clickable { viewModel.setHudTheme(theme.id) }
                                .testTag("theme_card_${theme.id}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(theme.primary, CircleShape)
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(theme.secondary, CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = theme.name.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) theme.primary else TextPrimary
                                )
                                Text(
                                    text = if (isSelected) "ACTIVE" else "STYLE #${theme.id + 1}",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) JarvisGreen else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: AI Orb Customization
        item {
            HudCard(title = "ORB SPECIFICATIONS") {
                Text(
                    text = "Adjust the physical scale and display coordinates of the holographic AI core.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Orb Size Selector
                Text(
                    text = "ORB DIMENSION: ${uiState.orbSize.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = hudTheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orbSizes.forEach { size ->
                        val isSelected = uiState.orbSize == size
                        OutlinedButton(
                            onClick = { viewModel.setOrbSize(size) },
                            modifier = Modifier.weight(1f).testTag("orb_size_$size"),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) hudTheme.primary else hudTheme.border
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) hudTheme.primary.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = size,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) hudTheme.primary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Orb Position
                Text(
                    text = "SCREEN ALIGNMENT: ${uiState.orbPosition.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = hudTheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orbPositions.forEach { pos ->
                        val isSelected = uiState.orbPosition == pos
                        OutlinedButton(
                            onClick = { viewModel.setOrbPosition(pos) },
                            modifier = Modifier.weight(1f).testTag("orb_pos_$pos"),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) hudTheme.primary else hudTheme.border
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) hudTheme.primary.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = pos,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) hudTheme.primary else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Shake Detection & Gesture Mapping
        item {
            HudCard(
                title = "ACCELEROMETER SHAKE SENSING",
                statusIndicator = if (uiState.isShakeEnabled) "ARMED" else "DISABLED"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SHAKE DETECTION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = "Triggers user-mapped action when rapid device motion is detected.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.isShakeEnabled,
                        onCheckedChange = { viewModel.setShakeEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = hudTheme.primary,
                            checkedTrackColor = hudTheme.primary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("shake_toggle_switch")
                    )
                }

                if (uiState.isShakeEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "SENSITIVITY THRESHOLD: ${uiState.shakeSensitivity} / 10",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = hudTheme.primary
                    )
                    Slider(
                        value = uiState.shakeSensitivity.toFloat(),
                        onValueChange = { viewModel.setShakeSensitivity(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = hudTheme.primary,
                            activeTrackColor = hudTheme.primary,
                            inactiveTrackColor = hudTheme.surface
                        ),
                        modifier = Modifier.testTag("shake_sensitivity_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ACTION ON SHAKE: ${uiState.shakeAction.uppercase()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        gestureActions.forEach { act ->
                            val isSel = uiState.shakeAction == act
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setShakeAction(act) },
                                label = { Text(act, fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = hudTheme.primary.copy(alpha = 0.25f),
                                    selectedLabelColor = hudTheme.primary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    borderColor = if (isSel) hudTheme.primary else hudTheme.border
                                ),
                                modifier = Modifier.testTag("shake_action_$act")
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Acoustic Clap / Sound Peak Detection
        item {
            HudCard(
                title = "ACOUSTIC CLAP SENSING",
                statusIndicator = if (uiState.isClapEnabled) "ACTIVE" else "OFF"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CLAP / SOUND PEAK DETECTOR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                        Text(
                            text = "Listens for sudden acoustic spikes with microphone permission.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.isClapEnabled,
                        onCheckedChange = { viewModel.setClapEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = hudTheme.primary,
                            checkedTrackColor = hudTheme.primary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("clap_toggle_switch")
                    )
                }

                if (uiState.isClapEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Sound Level Meter
                    Text(
                        text = "LIVE AUDIO AMPLITUDE METER",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { uiState.currentSoundLevel.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (uiState.currentSoundLevel > 0.6f) JarvisGreen else hudTheme.primary,
                        trackColor = hudTheme.surface
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CLAP SENSITIVITY: ${uiState.clapSensitivity} / 10",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = hudTheme.primary
                    )
                    Slider(
                        value = uiState.clapSensitivity.toFloat(),
                        onValueChange = { viewModel.setClapSensitivity(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = hudTheme.primary,
                            activeTrackColor = hudTheme.primary,
                            inactiveTrackColor = hudTheme.surface
                        ),
                        modifier = Modifier.testTag("clap_sensitivity_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ACTION ON CLAP: ${uiState.clapAction.uppercase()}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Voice Command", "Flashlight").forEach { act ->
                            val isSel = uiState.clapAction == act
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setClapAction(act) },
                                label = { Text(act, fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = hudTheme.primary.copy(alpha = 0.25f),
                                    selectedLabelColor = hudTheme.primary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSel,
                                    borderColor = if (isSel) hudTheme.primary else hudTheme.border
                                ),
                                modifier = Modifier.testTag("clap_action_$act")
                            )
                        }
                    }
                }
            }
        }
    }
}
