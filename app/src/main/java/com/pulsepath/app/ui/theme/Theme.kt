package com.pulsepath.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PulsePathColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = BackgroundWhite,
    secondary = BlueAccent,
    onSecondary = BackgroundWhite,
    tertiary = SuccessGreen,
    background = BackgroundWhite,
    onBackground = TextDark,
    surface = BackgroundWhite,
    onSurface = TextDark,
    surfaceVariant = SoftLavender,
    onSurfaceVariant = TextMuted,
    outline = CardBorder
)

@Composable
fun PulsePathTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PulsePathColorScheme,
        typography = Typography,
        content = content
    )
}