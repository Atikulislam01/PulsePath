package com.pulsepath.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.ArtImageFill
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.NavySubtitle
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.PulsePathTheme

@Composable
fun OnboardingScreen(
    onBack: () -> Unit,
    onNext: () -> Unit
) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Understand\nYour Digital Habits",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NavyHeading,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "See how you spend time on apps, manage your screen time, and build healthier habits.",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavySubtitle,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Your onboarding_illustration.png, or the placeholder
        ArtImageFill(name = "onboarding_illustration") { mod ->
            OnboardingIllustration(modifier = mod)
        }

        Spacer(modifier = Modifier.height(20.dp))

        PageDots(totalDots = 4, selectedIndex = 0)

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(text = "Next", onClick = onNext)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OnboardingIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF4F7FC)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3E4FA)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PhoneAndroid,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(110.dp)
            )
        }

        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (-80).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.BarChart,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(30.dp)
            )
        }

        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = 80.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFD6E6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = Color(0xFF1E6FE0),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun PageDots(totalDots: Int, selectedIndex: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .width(if (selected) 36.dp else 12.dp)
                    .clip(CircleShape)
                    .background(if (selected) PrimaryPurple else PrimaryPurple.copy(alpha = 0.5f))
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun OnboardingScreenPreview() {
    PulsePathTheme {
        OnboardingScreen(onBack = {}, onNext = {})
    }
}