package com.pulsepath.app.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.navigation.Routes
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.PulsePathTheme
import com.pulsepath.app.ui.theme.TextMuted

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

// Every tab has its own color
private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Rounded.Home, Color(0xFF1E90FF)),
    BottomNavItem(Routes.APPS, "Apps", Icons.Rounded.Apps, Color(0xFF7B61FF)),
    BottomNavItem(Routes.INSIGHTS, "Insights", Icons.Rounded.Insights, Color(0xFFFF8A00)),
    BottomNavItem(Routes.REPORTS, "Reports", Icons.Rounded.Description, Color(0xFF12A57F)),
    BottomNavItem(Routes.SETTINGS, "Settings", Icons.Rounded.Settings, Color(0xFFE5484D))
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundWhite)
    ) {
        HorizontalDivider(color = CardBorder)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            bottomNavItems.forEach { item ->
                NavItem(
                    item = item,
                    selected = item.route == currentRoute,
                    onClick = { onItemClick(item.route) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val pill by animateColorAsState(
        targetValue = if (selected) item.color.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(250),
        label = "pill"
    )
    val tint by animateColorAsState(
        targetValue = if (selected) item.color else TextMuted,
        animationSpec = tween(250),
        label = "tint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    Column(
        modifier = Modifier
            .bouncyClickable(onClick)
            .clip(RoundedCornerShape(18.dp))
            .background(pill)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier
                .size(26.dp)
                .scale(iconScale)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun BottomNavBarPreview() {
    PulsePathTheme {
        BottomNavBar(currentRoute = Routes.HOME, onItemClick = {})
    }
}