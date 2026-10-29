package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.navigation.JarvisNavHost
import com.example.ui.theme.JarvisAITheme
import com.example.viewmodel.JarvisViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            JarvisAITheme(hudThemeIndex = uiState.hudThemeIndex) {
                JarvisNavHost(viewModel = viewModel)
            }
        }
    }
}
