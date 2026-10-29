package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalHudTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisOrb(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioRms: Float,
    sizeDp: Dp = 190.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hudTheme = LocalHudTheme.current

    // Infinite rotation animations
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_rotation"
    )

    val rotationSlowRev by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slow_reverse_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Reactive audio expansion
    val reactiveAudioScale = if (isListening || isSpeaking) {
        1f + (audioRms * 0.45f)
    } else {
        pulseScale
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = sizeDp / 2),
                onClick = onClick
            )
            .testTag("jarvis_ai_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.82f * reactiveAudioScale

            // 1. Outer Glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        hudTheme.primary.copy(alpha = if (isListening) 0.5f else 0.25f),
                        hudTheme.glow.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.25f
                ),
                radius = baseRadius * 1.25f,
                center = center
            )

            // 2. Outermost rotating technical tick circle
            val ticks = 36
            for (i in 0 until ticks) {
                val angleRad = Math.toRadians((i * (360.0 / ticks) + rotationFast).toDouble())
                val isMajor = i % 4 == 0
                val tickLen = if (isMajor) 10f else 5f
                val r1 = baseRadius + 4f
                val r2 = r1 + tickLen
                val start = Offset(
                    (center.x + r1 * cos(angleRad)).toFloat(),
                    (center.y + r1 * sin(angleRad)).toFloat()
                )
                val end = Offset(
                    (center.x + r2 * cos(angleRad)).toFloat(),
                    (center.y + r2 * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = if (isMajor) hudTheme.primary else hudTheme.secondary.copy(alpha = 0.6f),
                    start = start,
                    end = end,
                    strokeWidth = if (isMajor) 2.5f else 1.2f
                )
            }

            // 3. Middle Segmented Arcs (Rotating Counter-clockwise)
            val strokeMid = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(45f, 18f, 15f, 18f), rotationSlowRev)
            )
            drawCircle(
                color = hudTheme.primary,
                radius = baseRadius * 0.85f,
                center = center,
                style = strokeMid
            )

            // 4. Secondary inner dashed guide ring
            drawCircle(
                color = hudTheme.secondary.copy(alpha = 0.5f),
                radius = baseRadius * 0.70f,
                center = center,
                style = Stroke(
                    width = 1.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), rotationFast * 0.5f)
                )
            )

            // 5. Audio Wave / Sine Visualizer when listening or speaking
            if (isListening || isSpeaking) {
                val wavePoints = 16
                for (j in 0 until wavePoints) {
                    val angle = Math.toRadians((j * (360.0 / wavePoints)).toDouble())
                    val barHeight = (baseRadius * 0.18f) * (0.3f + (audioRms * 1.2f) * ((j % 3 + 1) / 3f))
                    val innerR = baseRadius * 0.45f
                    val outerR = innerR + barHeight
                    val p1 = Offset(
                        (center.x + innerR * cos(angle)).toFloat(),
                        (center.y + innerR * sin(angle)).toFloat()
                    )
                    val p2 = Offset(
                        (center.x + outerR * cos(angle)).toFloat(),
                        (center.y + outerR * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = if (isListening) Color.White else hudTheme.primary,
                        start = p1,
                        end = p2,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 6. Glowing Inner Plasma Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        hudTheme.primary,
                        hudTheme.secondary,
                        hudTheme.glow.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 0.42f
                ),
                radius = baseRadius * 0.42f,
                center = center
            )

            // 7. Core bright pin-point
            drawCircle(
                color = Color.White,
                radius = 7f * reactiveAudioScale,
                center = center
            )
        }
    }
}
