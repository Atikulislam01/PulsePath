package com.pulsepath.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.ArtImage
import com.pulsepath.app.components.ArtImageFill
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.NavySubtitle
import com.pulsepath.app.ui.theme.PulsePathTheme

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Logo (your app_logo.png, or the placeholder "P")
        ArtImage(name = "app_logo", modifier = Modifier.size(96.dp)) {
            LogoMark()
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "PulsePath",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0B2ED6)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf("Understand", "Improve", "Grow").forEach { word ->
                Text(
                    text = word,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavySubtitle
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Your welcome_illustration.png, or the placeholder
        ArtImageFill(name = "welcome_illustration") { mod ->
            IllustrationPlaceholder(modifier = mod)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your digital behavior matters. Get insights, build better habits, and live a balanced life",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavySubtitle,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(text = "Get started", onClick = onGetStarted)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LogoMark() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E3CFF), Color(0xFFB02CF0))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "P",
            fontSize = 60.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
private fun IllustrationPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF4F7FC)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3ECFA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                tint = Color(0xFF1E40AF),
                modifier = Modifier.size(100.dp)
            )
        }

        FloatingBubble(Icons.Rounded.Schedule, Color(0xFFD6CCFF), Color(0xFF5B3FD1), 64.dp, (-90).dp, (-90).dp)
        FloatingBubble(Icons.Rounded.TrendingUp, Color(0xFFB4F5DC), Color(0xFF0E9F6E), 64.dp, 90.dp, (-60).dp)
        FloatingBubble(Icons.Rounded.ShoppingCart, Color(0xFFB4F0F5), Color(0xFF0891B2), 56.dp, 100.dp, 80.dp)
    }
}

@Composable
private fun FloatingBubble(
    icon: ImageVector,
    background: Color,
    tint: Color,
    size: Dp,
    x: Dp,
    y: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WelcomeScreenPreview() {
    PulsePathTheme {
        WelcomeScreen(onGetStarted = {})
    }
}