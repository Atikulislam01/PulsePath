package com.pulsepath.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted

private data class InfoSection(val title: String, val body: String)

private val transparencySections = listOf(
    InfoSection(
        "What PulsePath reads",
        "Using Android's Usage access permission, PulsePath reads which apps you open, when you open them, and for how long. " +
                "From this it works out your screen time, app sessions, app switches and categories."
    ),
    InfoSection(
        "What PulsePath does not read",
        "        \"It does not read your messages, passwords, photos, contacts or location. \" +\n" +
                "            \"It only knows that an app was open, not what you did in it. \" +\n" +
                "            \"If you switch on typing tracking, it counts how many characters you type and how fast, but never the characters themselves.\""
    ),
    InfoSection(
        "Where your data is stored",
        "Everything stays on this phone. PulsePath has no account, no server and no internet connection, so nothing is sent anywhere. " +
                "It saves a small daily history file of your sessions on the phone, so old days don't disappear when Android forgets them."
    ),
    InfoSection(
        "Your name",
        "If you enter a name, it is stored on this phone only and used for the greeting on Home."
    ),
    InfoSection(
        "Exports",
        "CSV and JSON files are created only when you tap Prepare export. You choose where they go through Android's share menu."
    ),
    InfoSection(
        "Your controls",
        "You can turn Monitoring off in Settings, remove Usage access in Android settings, delete the saved history in Privacy & Consent " +
                "(you can restore it for 30 days), or uninstall the app to remove everything. " +
                "Android keeps its own recent usage data, and PulsePath can't delete that."
    )
)

private val helpSections = listOf(
    InfoSection(
        "Home shows no data",
        "Open Android Settings → Apps → Special app access → Usage access → PulsePath and switch it on. " +
                "If the switch is greyed out, open Settings → Apps → PulsePath, tap the ⋮ menu and choose Allow restricted settings."
    ),
    InfoSection(
        "Numbers differ from Digital Wellbeing",
        "PulsePath works out sessions from Android's usage events, so the totals are close to Digital Wellbeing but won't match exactly."
    ),
    InfoSection(
        "Month or 30-day views look empty",
        "Android only keeps about a week of detailed history. PulsePath saves its own copy each day while Monitoring is on, " +
                "so older days fill in over time, starting from when you installed the app."
    ),
    InfoSection(
        "Reminders don't arrive",
        "Check that Notification Reminders and Enable Monitoring are on in Settings, and that notifications are allowed for PulsePath. " +
                "On Xiaomi, Oppo, Vivo and Samsung phones also set Settings → Apps → PulsePath → Battery → Unrestricted. " +
                "Reminders only appear between 7 am and 10 pm, at most once every 3 hours."
    ),
    InfoSection(
        "Categories look wrong",
        "Categories are guessed from the app's package name and what its developer declared, so some apps land in Other."
    ),
    InfoSection(
        "Exporting my data",
        "Go to Reports, choose a period and CSV or JSON, then tap Prepare export and pick where to send the file."
    )
)

@Composable
fun DataTransparencyScreen(onBack: () -> Unit) {
    InfoScreen(
        title = "Data Transparency",
        subtitle = "Exactly what PulsePath collects.",
        sections = transparencySections,
        onBack = onBack
    )
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    InfoScreen(
        title = "Help & Support",
        subtitle = "Answers to common questions.",
        sections = helpSections,
        onBack = onBack
    )
}

@Composable
private fun InfoScreen(
    title: String,
    subtitle: String,
    sections: List<InfoSection>,
    onBack: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .screenBackground()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackArrow(onClick = onBack)
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = screenTitleColor
            )
        }

        Text(
            text = subtitle,
            fontSize = 17.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        sections.forEach { section ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .cardBorder(2.dp, CardBorder, shape)
                    .padding(20.dp)
            ) {
                Text(
                    text = section.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = section.body,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = TextMuted
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}