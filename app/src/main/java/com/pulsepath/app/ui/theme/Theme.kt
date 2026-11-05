package com.pulsepath.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PulsePathTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = if (ThemeState.isDark) {
        darkColorScheme(
            primary = PrimaryPurple,
            onPrimary = Color.White,
            secondary = BlueAccent,
            onSecondary = Color.White,
            tertiary = SuccessGreen,
            background = BackgroundWhite,
            onBackground = TextDark,
            surface = Color(0xFF1A2038),
            onSurface = TextDark,
            surfaceVariant = SoftLavender,
            onSurfaceVariant = TextMuted,
            outline = CardBorder
        )
    } else {
        lightColorScheme(
            primary = PrimaryPurple,
            onPrimary = Color.White,
            secondary = BlueAccent,
            onSecondary = Color.White,
            tertiary = SuccessGreen,
            background = BackgroundWhite,
            onBackground = TextDark,
            surface = BackgroundWhite,
            onSurface = TextDark,
            surfaceVariant = SoftLavender,
            onSurfaceVariant = TextMuted,
            outline = CardBorder
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}