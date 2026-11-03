package com.pulsepath.app.screens

import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.DeleteDataDialog
import com.pulsepath.app.data.HistoryStore
import com.pulsepath.app.data.hasNotificationAccess
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.data.openNotificationSettings
import com.pulsepath.app.data.openUsageAccessSettings
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SuccessGreenStrong
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onExport: () -> Unit
) {
    val context = LocalContext.current
    var usageGranted by remember { mutableStateOf(hasUsageAccess(context)) }
    var notificationsGranted by remember { mutableStateOf(hasNotificationAccess(context)) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Re-check both permissions when the user comes back to the app
    val lifecycle = (context as? ComponentActivity)?.lifecycle
    DisposableEffect(lifecycle) {
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    usageGranted = hasUsageAccess(context)
                    notificationsGranted = hasNotificationAccess(context)
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    if (showDeleteDialog) {
        DeleteDataDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                val removed = HistoryStore.deleteAll(context)
                val message = if (removed == 0) {
                    "No saved history to delete yet."
                } else {
                    "Deleted $removed days. You can restore them for 30 days in Settings → Data recovery."
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackArrow(onClick = onBack)
            Text(
                text = "Privacy & Consent",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Text(
            text = "Your privacy is our priority.",
            fontSize = 17.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrivacyRow(
            icon = Icons.Rounded.Shield,
            color = Color(0xFF12A57F),
            title = "Usage Access",
            subtitle = if (usageGranted) "Granted" else "Not granted",
            onClick = if (usageGranted) null else ({ openUsageAccessSettings(context) })
        ) {
            if (usageGranted) GrantedCheck() else AllowText()
        }

        Spacer(modifier = Modifier.height(12.dp))

        PrivacyRow(
            icon = Icons.Rounded.Notifications,
            color = Color(0xFF1E90FF),
            title = "Notification Access",
            subtitle = if (notificationsGranted) "Granted" else "Not granted",
            onClick = if (notificationsGranted) null else ({ openNotificationSettings(context) })
        ) {
            if (notificationsGranted) GrantedCheck() else AllowText()
        }

        Spacer(modifier = Modifier.height(12.dp))

        PrivacyRow(
            icon = Icons.Rounded.Visibility,
            color = Color(0xFF0B6E99),
            title = "Data Transparency",
            subtitle = "Learn what we collect",
            onClick = { }
        ) {
            ChevronIcon()
        }

        Spacer(modifier = Modifier.height(28.dp))

        PrivacyRow(
            icon = Icons.Rounded.Delete,
            color = Color(0xFF1E90FF),
            title = "Delete My Data",
            subtitle = "Remove all collected data",
            onClick = { showDeleteDialog = true }
        ) {
            ChevronIcon()
        }

        Spacer(modifier = Modifier.height(12.dp))

        PrivacyRow(
            icon = Icons.Rounded.Description,
            color = Color(0xFF1E6FE0),
            title = "Export My Data",
            subtitle = "CSV/JSON/PDF",
            onClick = onExport
        ) {
            ChevronIcon()
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PrivacyRow(
    icon: ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .then(clickModifier)
            .padding(horizontal = 22.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
        }

        trailing()
    }
}

@Composable
private fun AllowText() {
    Text(
        text = "Allow",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryPurple
    )
}

@Composable
private fun GrantedCheck() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SuccessGreenStrong),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = "Granted",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ChevronIcon() {
    Icon(
        imageVector = Icons.Rounded.ChevronRight,
        contentDescription = null,
        tint = Color.Black,
        modifier = Modifier.size(30.dp)
    )
}