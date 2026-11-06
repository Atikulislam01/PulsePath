package com.pulsepath.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pulsepath.app.components.ArtImageFill
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.data.openUsageAccessSettings
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.NavySubtitle
import com.pulsepath.app.ui.theme.PrimaryPurpleDark
import com.pulsepath.app.ui.theme.PulsePathTheme
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.TextDark

@Composable
fun UsageAccessScreen(
    onBack: () -> Unit,
    onGranted: () -> Unit,
    onNotNow: () -> Unit
) {
    val context = LocalContext.current
    val currentOnGranted by rememberUpdatedState(onGranted)

    // true after the user taps "Grant Permission" and goes to Android settings
    var waitingForPermission by remember { mutableStateOf(false) }

    // When the user comes back from the settings page, check the permission
    val lifecycle = (context as? ComponentActivity)?.lifecycle
    DisposableEffect(lifecycle) {
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME &&
                    waitingForPermission &&
                    hasUsageAccess(context)
                ) {
                    waitingForPermission = false
                    currentOnGranted()
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            BackArrow(onClick = onBack)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Allow Usage Access",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NavyHeading,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "To monitor your app usage, we need permission to access usage statistics.",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavySubtitle,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Your permission_illustration.png, or the placeholder
        ArtImageFill(name = "permission_illustration") { mod ->
            ShieldIllustration(modifier = mod)
        }

        Spacer(modifier = Modifier.height(20.dp))

        WhatWeCollectCard()

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Grant Permission",
            onClick = {
                if (hasUsageAccess(context)) {
                    onGranted()
                } else {
                    waitingForPermission = true
                    openUsageAccessSettings(context)
                }
            }
        )

        Text(
            text = "Not Now",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurpleDark,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onNotNow)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun WhatWeCollectCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, CardBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "What we collect:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = com.pulsepath.app.ui.theme.InkBlack
        )

        Spacer(modifier = Modifier.height(12.dp))

        listOf("App usage duration", "App launch frequency", "Screen time data").forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item,
                    fontSize = 17.sp,
                    color = TextDark
                )
            }
        }
    }
}

private val BorderStrokeCard = BorderStroke(2.dp, CardBorder)

@Composable
private fun ShieldIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF4F7FC)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6ECFA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = Color(0xFF3B5BFF),
                modifier = Modifier.size(110.dp)
            )
        }

        AppBubble(Icons.Rounded.Chat, Color(0xFF22C55E), -120, -50)
        AppBubble(Icons.Rounded.MusicNote, Color(0xFF111A3A), 120, -50)
        AppBubble(null, Color(0xFF1877F2), -80, 60)
        AppBubble(Icons.Rounded.ShowChart, Color(0xFFFF8A00), 80, 60)
    }
}

@Composable
private fun AppBubble(icon: ImageVector?, color: Color, x: Int, y: Int) {
    Box(
        modifier = Modifier
            .offset(x = x.dp, y = y.dp)
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text(
                text = "f",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun UsageAccessScreenPreview() {
    PulsePathTheme {
        UsageAccessScreen(onBack = {}, onGranted = {}, onNotNow = {})
    }
}