package com.pulsepath.app.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.RecoverDataDialog
import com.pulsepath.app.data.HistoryStore
import com.pulsepath.app.data.ReminderScheduler
import com.pulsepath.app.data.SettingsPrefs
import com.pulsepath.app.data.hasNotificationAccess
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.data.openUsageAccessSettings
import com.pulsepath.app.data.showReminder
import com.pulsepath.app.ui.theme.AccentIndigo
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.SoftLavender
import com.pulsepath.app.ui.theme.SuccessGreenStrong
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManageCategories: () -> Unit,
    onExportData: () -> Unit
) {
    val context = LocalContext.current

    // These start from what was saved last time
    var monitoring by remember { mutableStateOf(SettingsPrefs.isMonitoringEnabled(context)) }
    var reminders by remember { mutableStateOf(SettingsPrefs.areRemindersEnabled(context)) }
    var granted by remember { mutableStateOf(hasUsageAccess(context)) }

    var showRecoverDialog by remember { mutableStateOf(false) }
    var storageVersion by remember { mutableIntStateOf(0) }   // bumps to refresh the saved-days count
    val savedDays = remember(storageVersion) { HistoryStore.storedDayCount(context) }

    // Android 13 and newer show a pop-up asking to allow notifications
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { allowed ->
        reminders = allowed
        SettingsPrefs.setRemindersEnabled(context, allowed)
        if (!allowed) {
            Toast.makeText(
                context,
                "Notifications are blocked. Allow them in Android settings to get reminders.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Re-check the permission and the saved days whenever the user comes back to the app
    val lifecycle = (context as? ComponentActivity)?.lifecycle
    DisposableEffect(lifecycle) {
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    granted = hasUsageAccess(context)
                    storageVersion++
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    if (showRecoverDialog) {
        RecoverDataDialog(
            dayCount = HistoryStore.recoverableDayCount(context),
            daysLeft = HistoryStore.recoveryDaysLeft(context),
            onDismiss = { showRecoverDialog = false },
            onRestore = {
                showRecoverDialog = false
                val restored = HistoryStore.restoreDeleted(context)
                storageVersion++
                Toast.makeText(
                    context,
                    if (restored > 0) "Restored $restored days of history." else "Nothing to restore.",
                    Toast.LENGTH_LONG
                ).show()
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
                text = "Settings",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Text(
            text = "Manage your preference and data.",
            fontSize = 17.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsCard(title = "Monitoring") {
            SettingsRow(label = "Enable Monitoring") {
                PulseSwitch(
                    checked = monitoring,
                    onChange = {
                        monitoring = it
                        SettingsPrefs.setMonitoringEnabled(context, it)
                        ReminderScheduler.sync(context)
                    }
                )
            }
            HorizontalDivider(color = CardBorder)
            SettingsRow(
                label = "Usage Access Permission",
                onClick = if (granted) null else ({ openUsageAccessSettings(context) })
            ) {
                Text(
                    text = if (granted) "Granted" else "Not granted",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (granted) SuccessGreenStrong else Color(0xFFE5484D)
                )
            }
            HorizontalDivider(color = CardBorder)
            SettingsRow(label = "Notification Reminders") {
                PulseSwitch(
                    checked = reminders,
                    onChange = { wantOn ->
                        if (wantOn &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !hasNotificationAccess(context)
                        ) {
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            reminders = wantOn
                            SettingsPrefs.setRemindersEnabled(context, wantOn)
                        }
                    }
                )
            }
            HorizontalDivider(color = CardBorder)
            SettingsRow(
                label = "Send a test reminder",
                onClick = {
                    val sent = showReminder(
                        context,
                        title = "Test reminder",
                        text = "This is how PulsePath reminders will look."
                    )
                    if (!sent) {
                        Toast.makeText(
                            context,
                            "Notifications are blocked. Allow them in Android settings first.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            ) {
                Chevron()
            }
        }

        Text(
            text = "While Monitoring is on, PulsePath saves your history in the background about every 15 minutes. Reminders appear when you open the same app 10 times in 2 hours, at most once every 3 hours and never between 10 pm and 7 am.",
            fontSize = 13.sp,
            color = TextMuted,
            lineHeight = 18.sp,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsCard(title = "App Categories") {
            SettingsRow(label = "Manage app categories", onClick = onManageCategories) {
                Chevron()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsCard(title = "Data & Storage") {
            SettingsRow(label = "Saved history") {
                Text(
                    text = if (savedDays == 1) "1 day" else "$savedDays days",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            HorizontalDivider(color = CardBorder)
            SettingsRow(label = "Data recovery", onClick = { showRecoverDialog = true }) {
                Text(
                    text = "(30 days)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Chevron()
            }
            HorizontalDivider(color = CardBorder)
            SettingsRow(label = "Export Data", onClick = onExportData) {
                Chevron()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsCard(title = "Other") {
            SettingsRow(label = "Help & Support", onClick = { }) {
                Chevron()
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 12.dp)
        )
        HorizontalDivider(color = CardBorder)
        content()
    }
}

@Composable
private fun SettingsRow(
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            color = Color.Black
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            trailing()
        }
    }
}

@Composable
private fun PulseSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AccentIndigo,
            checkedBorderColor = AccentIndigo,
            uncheckedThumbColor = TextMuted,
            uncheckedTrackColor = SoftLavender,
            uncheckedBorderColor = CardBorder
        )
    )
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.Rounded.ChevronRight,
        contentDescription = null,
        tint = Color.Black
    )
}