package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.components.HudCard
import com.example.ui.theme.*
import com.example.viewmodel.JarvisUiState
import com.example.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversationScreen(
    viewModel: JarvisViewModel,
    uiState: JarvisUiState,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val messages by viewModel.chatMessages.collectAsState(emptyList())
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Diagnostics",
        "Toggle Flashlight",
        "Weather report",
        "Who are you?",
        "नमस्ते जारविस",
        "Drink water reminder",
        "Open YouTube"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(hudTheme.backgroundStart)
            .padding(bottom = 85.dp)
    ) {
        // Top Toolbar
        Surface(
            color = hudTheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, hudTheme.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${uiState.assistantName} VOCAL INTERFACE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = hudTheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "LANGUAGE: ${if (uiState.language == "hi") "HINDI (हिन्दी)" else "ENGLISH (US)"}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Toggle
                    FilledTonalButton(
                        onClick = {
                            viewModel.setLanguage(if (uiState.language == "hi") "en" else "hi")
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = hudTheme.border,
                            contentColor = hudTheme.primary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("toggle_language_button")
                    ) {
                        Text(
                            text = if (uiState.language == "hi") "EN" else "HI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Clear History
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = {
                        inputText = prompt
                        viewModel.submitUserQuery(prompt)
                        inputText = ""
                    },
                    label = {
                        Text(
                            text = prompt,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = hudTheme.primary
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = hudTheme.surface
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = hudTheme.border
                    ),
                    modifier = Modifier.testTag("chip_${prompt.lowercase().replace(" ", "_")}")
                )
            }
        }

        // Conversation Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = "Standby",
                            tint = hudTheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NEURAL LINK ESTABLISHED",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = hudTheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Type a command below or tap the microphone to engage ${uiState.assistantName}.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg, assistantName = uiState.assistantName)
                }
            }
        }

        // Active Voice / Mic Indicator Bar
        AnimatedVisibility(visible = uiState.isListening || uiState.isSpeaking) {
            Surface(
                color = hudTheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (uiState.isListening) JarvisRed else JarvisGreen,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isListening) "MIC ACTIVE: LISTENING..." else "AUDIO OUTPUT PLAYING...",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                }
            }
        }

        // Bottom Text & Voice Input Bar
        Surface(
            color = hudTheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, hudTheme.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Command ${uiState.assistantName}...",
                            fontSize = 13.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = hudTheme.primary,
                        unfocusedBorderColor = hudTheme.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = hudTheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.submitUserQuery(inputText)
                            inputText = ""
                        }
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Microphone Voice Button
                IconButton(
                    onClick = {
                        if (uiState.isListening) {
                            viewModel.stopVoiceListening()
                        } else {
                            viewModel.startVoiceListening()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (uiState.isListening) JarvisRed else hudTheme.border,
                            CircleShape
                        )
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (uiState.isListening) Color.White else hudTheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.submitUserQuery(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (inputText.isNotBlank()) hudTheme.primary else hudTheme.surface,
                            CircleShape
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.Black else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, assistantName: String) {
    val hudTheme = LocalHudTheme.current
    val isUser = message.sender == MessageSender.USER
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp,
                bottomStart = if (isUser) 10.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 10.dp
            ),
            color = if (isUser) hudTheme.surface.copy(alpha = 0.9f) else hudTheme.backgroundEnd,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) hudTheme.border else hudTheme.primary.copy(alpha = 0.5f)
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "OPERATOR" else assistantName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isUser) TextSecondary else hudTheme.primary
                    )
                    Text(
                        text = timeStr,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
