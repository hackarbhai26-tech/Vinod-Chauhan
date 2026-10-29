package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConfirmationDialog
import com.example.ui.screens.*
import com.example.ui.theme.LocalHudTheme
import com.example.viewmodel.JarvisViewModel
import com.example.viewmodel.PendingAction

enum class JarvisScreen(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("HUD", Icons.Default.Dashboard),
    CONVERSATION("DISPATCH", Icons.Default.RecordVoiceOver),
    LAB("LAB", Icons.Default.Science),
    CONTROLS("DEVICE", Icons.Default.Tune),
    FILES("STORAGE", Icons.Default.Folder),
    APK_DOWNLOAD("APK", Icons.Default.Download),
    SECURITY("SECURITY", Icons.Default.Security)
}

@Composable
fun JarvisNavHost(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val uiState by viewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf(JarvisScreen.DASHBOARD) }

    // Predictive back handler
    if (currentScreen != JarvisScreen.DASHBOARD) {
        BackHandler {
            currentScreen = JarvisScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = hudTheme.backgroundStart,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = hudTheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, hudTheme.border),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                val navItems = listOf(
                    JarvisScreen.DASHBOARD,
                    JarvisScreen.CONVERSATION,
                    JarvisScreen.LAB,
                    JarvisScreen.CONTROLS,
                    JarvisScreen.FILES,
                    JarvisScreen.APK_DOWNLOAD,
                    JarvisScreen.SECURITY
                )

                navItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) hudTheme.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) hudTheme.primary else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = hudTheme.primary,
                            selectedTextColor = hudTheme.primary,
                            indicatorColor = hudTheme.primary.copy(alpha = 0.2f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                JarvisScreen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onNavigateToConversation = { currentScreen = JarvisScreen.CONVERSATION }
                )
                JarvisScreen.CONVERSATION -> ConversationScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                JarvisScreen.LAB -> LabGesturesScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                JarvisScreen.CONTROLS -> DeviceControlScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                JarvisScreen.FILES -> FileManagerScreen(
                    viewModel = viewModel
                )
                JarvisScreen.APK_DOWNLOAD -> ApkDownloadScreen()
                JarvisScreen.SECURITY -> PrivacySecurityScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            // Global Confirmation Dialog for Consequential Actions
            uiState.pendingActionDialog?.let { action ->
                when (action) {
                    is PendingAction.CallAction -> {
                        ConfirmationDialog(
                            title = "CONFIRM PHONE DIAL DIRECTIVE",
                            message = "JARVIS is preparing to place a telephone call to: '${action.contactOrNumber}'. Proceed with launching Android dialer?",
                            confirmLabel = "DIAL CALL",
                            onConfirm = { viewModel.confirmPendingAction() },
                            onDismiss = { viewModel.dismissPendingAction() }
                        )
                    }
                    is PendingAction.WhatsAppAction -> {
                        ConfirmationDialog(
                            title = "CONFIRM WHATSAPP DISPATCH",
                            message = "JARVIS is preparing to dispatch the following transmission via WhatsApp:\n\n\"${action.message}\"\n\nLaunch WhatsApp interface?",
                            confirmLabel = "DISPATCH",
                            onConfirm = { viewModel.confirmPendingAction() },
                            onDismiss = { viewModel.dismissPendingAction() }
                        )
                    }
                    is PendingAction.DeleteFileAction -> {
                        ConfirmationDialog(
                            title = "PERMANENT FILE DELETION",
                            message = "Are you sure you want to permanently erase '${action.file.name}' from storage? This action cannot be reversed.",
                            confirmLabel = "DELETE FILE",
                            isDestructive = true,
                            onConfirm = { viewModel.confirmPendingAction() },
                            onDismiss = { viewModel.dismissPendingAction() }
                        )
                    }
                }
            }
        }
    }
}
