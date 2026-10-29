package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalHudTheme
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TerminalLog(
    val id: String = "${System.nanoTime()}",
    val text: String,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CommandTerminal(
    logs: List<TerminalLog>,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current
    val listState = rememberLazyListState()
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                val timeStr = timeFormat.format(Date(log.timestamp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "[$timeStr] ",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                    Text(
                        text = if (log.isSystem) ">> ${log.text}" else "> ${log.text}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (log.isSystem) hudTheme.secondary else hudTheme.primary
                    )
                }
            }
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = ">> JARVIS Core v4.8 online. Awaiting operator input...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = hudTheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
