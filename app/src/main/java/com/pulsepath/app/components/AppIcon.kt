package com.pulsepath.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.ui.theme.PrimaryPurple

private data class IconStyle(
    val brush: Brush,
    val icon: ImageVector?,
    val letter: String?,
    val contentColor: Color
)

private fun iconStyleFor(appName: String): IconStyle = when (appName) {
    "YouTube" -> IconStyle(SolidColor(Color(0xFFFF0000)), Icons.Rounded.PlayArrow, null, Color.White)
    "WhatsApp" -> IconStyle(SolidColor(Color(0xFF25D366)), Icons.Rounded.Chat, null, Color.White)
    "Instagram" -> IconStyle(
        Brush.linearGradient(listOf(Color(0xFF8A3AB9), Color(0xFFE95950), Color(0xFFFCCC63))),
        Icons.Rounded.CameraAlt, null, Color.White
    )
    "TikTok" -> IconStyle(SolidColor(Color(0xFF111111)), Icons.Rounded.MusicNote, null, Color.White)
    "Facebook" -> IconStyle(SolidColor(Color(0xFF1877F2)), null, "f", Color.White)
    "Chrome" -> IconStyle(SolidColor(Color(0xFF4285F4)), Icons.Rounded.Public, null, Color.White)
    "Snapchat" -> IconStyle(SolidColor(Color(0xFFFFFC00)), null, "S", com.pulsepath.app.ui.theme.InkBlack)
    else -> IconStyle(SolidColor(PrimaryPurple), null, appName.take(1), Color.White)
}

/** A colored app badge. Later we can swap these for real app icons. */
@Composable
fun AppIcon(
    appName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val style = iconStyleFor(appName)
    val icon = style.icon
    val shape = if (appName == "Facebook" || appName == "Chrome") {
        CircleShape
    } else {
        RoundedCornerShape(size * 0.25f)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(style.brush),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = appName,
                tint = style.contentColor,
                modifier = Modifier.size(size * 0.55f)
            )
        } else {
            Text(
                text = style.letter ?: "",
                fontSize = (size.value * 0.5f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = style.contentColor
            )
        }
    }
}
