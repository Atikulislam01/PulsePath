package com.pulsepath.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PackageAppIcon
import com.pulsepath.app.data.UsageRepository
import com.pulsepath.app.data.UsageSessionRecord
import com.pulsepath.app.data.UsageSnapshot
import com.pulsepath.app.data.formatClock
import com.pulsepath.app.data.formatDuration
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SoftBlueBg
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AppSessionTimelineScreen(
    onBack: () -> Unit,
    onSessionClick: (String) -> Unit   // receives the app's package name
) {
    val context = LocalContext.current
    val granted = hasUsageAccess(context)

    var date by remember { mutableStateOf(LocalDate.now()) }
    val isToday = !date.isBefore(LocalDate.now())
    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH) }
    val dateShape = RoundedCornerShape(28.dp)

    // Load the selected day on a background thread
    val snapshot by produceState<UsageSnapshot?>(initialValue = null, date, granted) {
        value = null
        value = if (granted) {
            withContext(Dispatchers.IO) { UsageRepository.loadDay(context, date) }
        } else {
            UsageSnapshot(0L, emptyList(), emptyList())
        }
    }
    val snap = snapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackArrow(onClick = onBack)
            Text(
                text = "App Session Timeline",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Text(
            text = "View your app usage sessions in detail.",
            fontSize = 17.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Date selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dateShape)
                .background(SoftBlueBg)
                .border(2.dp, CardBorder, dateShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { date = date.minusDays(1) }) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Previous day",
                    tint = TextDark
                )
            }
            Text(
                text = date.format(formatter),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            IconButton(onClick = { if (!isToday) date = date.plusDays(1) }) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Next day",
                    tint = if (isToday) TextMuted.copy(alpha = 0.4f) else TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            !granted -> CenterMessage(
                "Allow usage access in Settings to see your app sessions."
            )

            snap == null -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }

            else -> {
                // Ignore very short sessions (under 10 seconds) to keep the list readable
                val sessions = snap.sessions.filter { it.durationMillis >= 10_000L }

                if (sessions.isEmpty()) {
                    CenterMessage("No app sessions recorded on this day.")
                } else {
                    val names = snap.apps.associate { it.packageName to it.appName }

                    Text(
                        text = "${formatDuration(snap.totalMillis)} total  •  ${sessions.size} sessions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(sessions) { index, session ->
                            SessionRow(
                                session = session,
                                appName = names[session.packageName] ?: session.packageName,
                                onClick = { onSessionClick(session.packageName) }
                            )
                            if (index < sessions.lastIndex) {
                                HorizontalDivider(color = CardBorder)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.CenterMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun SessionRow(
    session: UsageSessionRecord,
    appName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PackageAppIcon(packageName = session.packageName, appName = appName, size = 52.dp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${formatClock(session.startMillis)} - ${formatClock(session.endMillis)}",
                fontSize = 15.sp,
                color = TextMuted
            )
        }

        Text(
            text = formatDuration(session.durationMillis),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}