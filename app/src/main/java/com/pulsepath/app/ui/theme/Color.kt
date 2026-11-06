package com.pulsepath.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** The current mode. Changing it redraws every screen that uses the colors below. */
object ThemeState {
    var isDark by mutableStateOf(false)
}

private fun pick(light: Long, dark: Long): Color =
    Color(if (ThemeState.isDark) dark else light)

// ---- Brand colors (same in both modes) ----
val PrimaryPurple = Color(0xFF7B61FF)
val AccentIndigo = Color(0xFF4F46E5)
val SuccessGreen = Color(0xFF12A57F)
val SuccessGreenStrong = Color(0xFF2DB052)
val BlueAccent = Color(0xFF1E90FF)
val CyanAccent = Color(0xFF00C2FF)

// ---- Colors that change with the mode ----
val PrimaryPurpleDark: Color get() = pick(0xFF5B3FD1, 0xFFB7A6FF)

val NavyHeading: Color get() = pick(0xFF263A73, 0xFFC9D3FF)
val NavySubtitle: Color get() = pick(0xFF36568F, 0xFF9FB0E0)
val TextDark: Color get() = pick(0xFF1A2340, 0xFFE8ECF8)
val TextMuted: Color get() = pick(0xFF6B7A99, 0xFF9AA6C6)

/** Replaces com.pulsepath.app.ui.theme.InkBlack for text and icons. */
val InkBlack: Color get() = pick(0xFF000000, 0xFFF1F4FF)

val SuccessGreenBg: Color get() = pick(0xFFDDF3EE, 0xFF123A32)

val BackgroundWhite: Color get() = pick(0xFFFFFFFF, 0xFF0F1424)
val CardBorder: Color get() = pick(0xFFDDE3EE, 0xFF2B3350)
val SoftLavender: Color get() = pick(0xFFEEF0FF, 0xFF1E2542)
val SoftBlueBg: Color get() = pick(0xFFF0F4FF, 0xFF182038)

// ---- Chart colors (same in both modes) ----
val ChartBlueTop = Color(0xFF3F51F5)
val ChartCyanBottom = Color(0xFF00D4FF)
val ChartPink = Color(0xFFFF6FB5)
val ChartTeal = Color(0xFF17C3CE)