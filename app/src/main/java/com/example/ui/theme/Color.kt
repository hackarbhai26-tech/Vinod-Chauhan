package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default Neon Cyan Palette
val JarvisCyan = Color(0xFF00E5FF)
val JarvisCyanBright = Color(0xFF80F7FF)
val JarvisCyanGlow = Color(0x6600E5FF)
val JarvisBlueDark = Color(0xFF005288)
val JarvisBackgroundDark = Color(0xFF030712)
val JarvisBackgroundSurface = Color(0xFF071120)
val JarvisCardBg = Color(0xE60A182F)
val JarvisBorder = Color(0x4000E5FF)

val TextPrimary = Color(0xFFE2F1FF)
val TextSecondary = Color(0xFF82A0C2)
val TextMuted = Color(0xFF4A627A)

// Alert and Accent Colors
val JarvisGreen = Color(0xFF00E676)
val JarvisAmber = Color(0xFFFFB300)
val JarvisRed = Color(0xFFFF1744)
val JarvisPurple = Color(0xFFD500F9)

// 14 Futuristic HUD Themes
data class HudThemeDefinition(
    val id: Int,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val glow: Color,
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val surface: Color,
    val border: Color
)

val HudThemes = listOf(
    HudThemeDefinition(0, "Arc Reactor", Color(0xFF00E5FF), Color(0xFF0091EA), Color(0x6600E5FF), Color(0xFF020713), Color(0xFF061426), Color(0xFF0A1E38), Color(0x5500E5FF)),
    HudThemeDefinition(1, "Hologram Blue", Color(0xFF40C4FF), Color(0xFF00B0FF), Color(0x6640C4FF), Color(0xFF020914), Color(0xFF04172C), Color(0xFF072444), Color(0x5540C4FF)),
    HudThemeDefinition(2, "Cyberpunk Amber", Color(0xFFFFD600), Color(0xFFFF9100), Color(0x66FFD600), Color(0xFF0F0B01), Color(0xFF1F1502), Color(0xFF2C1F05), Color(0x55FFD600)),
    HudThemeDefinition(3, "Deep Space Neon", Color(0xFF00B0FF), Color(0xFF651FFF), Color(0x6600B0FF), Color(0xFF040314), Color(0xFF0B0924), Color(0xFF14103B), Color(0x55651FFF)),
    HudThemeDefinition(4, "Quantum Matrix", Color(0xFF00E676), Color(0xFF00C853), Color(0x6600E676), Color(0xFF020F06), Color(0xFF041F0C), Color(0xFF072C13), Color(0x5500E676)),
    HudThemeDefinition(5, "Stark Titanium", Color(0xFFE0E0E0), Color(0xFF90A4AE), Color(0x66CFD8DC), Color(0xFF0A0C0E), Color(0xFF14181C), Color(0xFF1F242A), Color(0x55B0BEC5)),
    HudThemeDefinition(6, "Crimson Alert", Color(0xFFFF1744), Color(0xFFD50000), Color(0x66FF1744), Color(0xFF140205), Color(0xFF26040A), Color(0xFF380811), Color(0x55FF1744)),
    HudThemeDefinition(7, "Bio-Synthesizer", Color(0xFF76FF03), Color(0xFF64DD17), Color(0x6676FF03), Color(0xFF040F02), Color(0xFF0B1E05), Color(0xFF122C09), Color(0x5576FF03)),
    HudThemeDefinition(8, "Plasma Core", Color(0xFFFF4081), Color(0xFFE040FB), Color(0x66FF4081), Color(0xFF12030B), Color(0xFF240616), Color(0xFF360A22), Color(0x55FF4081)),
    HudThemeDefinition(9, "Orbit Station", Color(0xFF18FFFF), Color(0xFF536DFE), Color(0x6618FFFF), Color(0xFF020917), Color(0xFF06152E), Color(0xFF0C2046), Color(0x5518FFFF)),
    HudThemeDefinition(10, "Nebula Violet", Color(0xFFE040FB), Color(0xFF7C4DFF), Color(0x66E040FB), Color(0xFF0F0318), Color(0xFF1D062E), Color(0xFF2B0A42), Color(0x55E040FB)),
    HudThemeDefinition(11, "Golden Sol", Color(0xFFFFAB00), Color(0xFFFF6D00), Color(0x66FFAB00), Color(0xFF120902), Color(0xFF211104), Color(0xFF331C08), Color(0x55FFAB00)),
    HudThemeDefinition(12, "Stealth Shadow", Color(0xFFFFFFFF), Color(0xFFB0BEC5), Color(0x44FFFFFF), Color(0xFF050505), Color(0xFF0D0E10), Color(0xFF15181C), Color(0x44FFFFFF)),
    HudThemeDefinition(13, "Solar Flare", Color(0xFFFF6E40), Color(0xFFFF3D00), Color(0x66FF6E40), Color(0xFF140702), Color(0xFF240E04), Color(0xFF361608), Color(0x55FF6E40))
)
