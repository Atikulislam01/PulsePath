package com.pulsepath.app.components

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.pulsepath.app.data.TypingTracker
import com.pulsepath.app.data.appLabel
import com.pulsepath.app.data.isTypingServiceEnabled
import com.pulsepath.app.data.openAccessibilitySettings
import com.pulsepath.app.ui.theme.AccentIndigo
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SoftLavender
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlin.math.roundToInt

private val ErrorRed = Color(0xFFE5484D)

/** "Typing in other apps": consent, on/off and the results of the last 7 days. */
@Composable
fun SystemTypingSection() {
    val context = LocalContext.current
    val shape = RoundedCornerShape(28.dp)

    var serviceOn by remember { mutableStateOf(isTypingServiceEnabled(context)) }
    var tracking by remember { mutableStateOf(TypingTracker.isEnabled(context)) }
    var refresh by remember { mutableIntStateOf(0) }
    var showConsent by remember { mutableStateOf(false) }

    // Re-check when the user comes back from Android's settings
    val lifecycle = (context as? ComponentActivity)?.lifecycle
    DisposableEffect(lifecycle) {
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    serviceOn = isTypingServiceEnabled(context)
                    tracking = TypingTracker.isEnabled(context)
                    refresh++
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    val apps = remember(refresh) { TypingTracker.loadApps(context, 7) }
    val totalChars = apps.sumOf { it.chars }
    val totalMillis = apps.sumOf { it.millis }
    val overallWpm = if (totalMillis > 0L) {
        ((totalChars / 5.0) / (totalMillis / 60_000.0)).roundToInt()
    } else {
        0
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("Track typing in other apps?") },
            text = {
                Text(
                    "PulsePath will count how many characters you type and how fast, in any app.\n\n" +
                            "• It never reads or saves what you type.\n" +
                            "• Password fields are ignored.\n" +
                            "• Only numbers are saved, on this phone only.\n\n" +
                            "Android will now open Accessibility settings. Find PulsePath and switch it on. " +
                            "Android shows a strong warning for this permission. It is normal for any app that uses it."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConsent = false
                    TypingTracker.setEnabled(context, true)
                    tracking = true
                    openAccessibilitySettings(context)
                }) {
                    Text("I agree", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsent = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(24.dp)
    ) {
        Text(
            text = "Typing in other apps",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (!serviceOn) {
            Text(
                text = "See how fast you type in WhatsApp, Chrome and every other app. " +
                        "PulsePath only counts characters and time. It never reads what you type.",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(text = "Set up", onClick = { showConsent = true })
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "If the Accessibility switch is greyed out: Settings → Apps → PulsePath → ⋮ menu → Allow restricted settings.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextMuted
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (tracking) "Tracking is on" else "Tracking is paused",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (tracking) SuccessGreen else TextMuted
                )
                Switch(
                    checked = tracking,
                    onCheckedChange = {
                        tracking = it
                        TypingTracker.setEnabled(context, it)
                    },
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

            Spacer(modifier = Modifier.height(12.dp))

            if (apps.isEmpty()) {
                Text(
                    text = "No typing recorded yet. Type a message in any app, then come back.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = TextMuted
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Tile("Last 7 days", "$overallWpm WPM", Modifier.weight(1f))
                    Tile("Characters", totalChars.toString(), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                apps.take(5).forEach { app ->
                    val name = remember(app.packageName) { appLabel(context, app.packageName) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PackageAppIcon(packageName = app.packageName, appName = name, size = 36.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${app.wpm} WPM  •  ${app.chars} chars",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                }

                TextButton(onClick = {
                    TypingTracker.clear(context)
                    refresh++
                    Toast.makeText(context, "Typing activity deleted", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete typing activity", color = ErrorRed)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "To stop completely, switch PulsePath off in Android's Accessibility settings.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextMuted
            )
            TextButton(onClick = { openAccessibilitySettings(context) }) {
                Text("Open Accessibility settings", color = PrimaryPurple)
            }
        }
    }
}

@Composable
private fun Tile(label: String, value: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(14.dp)
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
    }
}