package com.pulsepath.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.ArtImageFill
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.NavySubtitle
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.PrimaryPurpleDark
import com.pulsepath.app.ui.theme.PulsePathTheme
import com.pulsepath.app.ui.theme.SoftBlueBg

@Composable
fun NoDataScreen(
    onBack: () -> Unit,
    onStartMonitoring: () -> Unit,
    onLearnMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground()
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
            text = "No Data Yet",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = screenTitleColor
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Start using the app to see your behaviour insights here.",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavySubtitle,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Your nodata_illustration.png, or the placeholder
        ArtImageFill(name = "nodata_illustration") { mod ->
            NoDataPlaceholder(modifier = mod)
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(text = "Start Monitoring", onClick = onStartMonitoring)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLearnMore,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, PrimaryPurple),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SoftBlueBg,
                contentColor = PrimaryPurpleDark
            )
        ) {
            Text(
                text = "Learn More",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NoDataPlaceholder(modifier: Modifier = Modifier) {
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
                imageVector = Icons.Rounded.Insights,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(100.dp)
            )
        }
        SmallBubble(Icons.Rounded.Favorite, Color(0xFF8E9CFF), (-110).dp, (-70).dp)
        SmallBubble(Icons.Rounded.Person, Color(0xFF8E9CFF), 110.dp, (-70).dp)
    }
}

@Composable
private fun SmallBubble(
    icon: ImageVector,
    tint: Color,
    x: Dp,
    y: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun NoDataPreview() {
    PulsePathTheme {
        NoDataScreen(onBack = {}, onStartMonitoring = {}, onLearnMore = {})
    }
}