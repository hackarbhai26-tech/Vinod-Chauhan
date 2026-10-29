package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalHudTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun HudCard(
    title: String? = null,
    statusIndicator: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val hudTheme = LocalHudTheme.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Corner tech brackets
                val bLen = 14f
                val stroke = 3f
                // Top-Left
                drawLine(hudTheme.primary, Offset(0f, 0f), Offset(bLen, 0f), stroke)
                drawLine(hudTheme.primary, Offset(0f, 0f), Offset(0f, bLen), stroke)
                // Top-Right
                drawLine(hudTheme.primary, Offset(size.width, 0f), Offset(size.width - bLen, 0f), stroke)
                drawLine(hudTheme.primary, Offset(size.width, 0f), Offset(size.width, bLen), stroke)
                // Bottom-Left
                drawLine(hudTheme.primary, Offset(0f, size.height), Offset(bLen, size.height), stroke)
                drawLine(hudTheme.primary, Offset(0f, size.height), Offset(0f, size.height - bLen), stroke)
                // Bottom-Right
                drawLine(hudTheme.primary, Offset(size.width, size.height), Offset(size.width - bLen, size.height), stroke)
                drawLine(hudTheme.primary, Offset(size.width, size.height), Offset(size.width, size.height - bLen), stroke)
            },
        shape = RoundedCornerShape(8.dp),
        color = hudTheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, hudTheme.border),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(hudTheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            color = hudTheme.primary
                        )
                    }
                    if (statusIndicator != null) {
                        Text(
                            text = statusIndicator.uppercase(),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                    }
                }
            }
            content()
        }
    }
}
