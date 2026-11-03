package com.pulsepath.app.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.data.ExportFormat
import com.pulsepath.app.data.ReportData
import com.pulsepath.app.data.createExportFile
import com.pulsepath.app.data.formatDuration
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.data.loadReport
import com.pulsepath.app.data.shareExportFile
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.SuccessGreenBg
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Label and number of days for each report period
private val periodOptions = listOf(
    "Last 7 days" to 7,
    "Last 30 days" to 30,
    "Today" to 1
)

private data class ExportOption(
    val title: String,
    val subtitle: String,
    val badge: String,
    val icon: ImageVector,
    val color: Color,
    val enabled: Boolean
)

private val exportOptions = listOf(
    ExportOption("CSV", "Spreadsheet-ready table", "Recommended", Icons.Rounded.TableChart, Color(0xFF4F46E5), true),
    ExportOption("JSON", "Structured metadata", "Developer-friendly", Icons.Rounded.Code, Color(0xFF1E90FF), true),
    ExportOption("Summary PDF", "Readable report", "Coming soon", Icons.Rounded.PictureAsPdf, Color(0xFF6B7A99), false)
)

@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    onOpenWeekly: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenNoData: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val granted = hasUsageAccess(context)

    var periodIndex by remember { mutableIntStateOf(0) }
    var selectedExport by remember { mutableIntStateOf(0) }
    var exporting by remember { mutableStateOf(false) }

    val periodLabel = periodOptions[periodIndex].first
    val days = periodOptions[periodIndex].second
    val cardShape = RoundedCornerShape(28.dp)

    // Load the report on a background thread
    val report by produceState<ReportData?>(initialValue = null, days, granted) {
        value = null
        value = if (granted) {
            withContext(Dispatchers.IO) { loadReport(context, days) }
        } else {
            null
        }
    }
    val r = report

    // Text for the four summary tiles
    fun stat(read: (ReportData) -> String): String = when {
        r != null -> read(r)
        !granted -> "0"
        else -> "…"
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
                text = "Report & Exports",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Report period card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(2.dp, CardBorder, cardShape)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "REPORT PERIOD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = periodLabel,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SuccessGreenBg)
                    .clickable { periodIndex = (periodIndex + 1) % periodOptions.size }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Change ▾",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (days == 7) "Weekly summary" else "Summary",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile("Total screen time", stat { formatDuration(it.snapshot.totalMillis) }, Modifier.weight(1f))
            StatTile("Apps used", stat { it.snapshot.apps.size.toString() }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile("App sessions", stat { it.snapshot.sessions.size.toString() }, Modifier.weight(1f))
            StatTile("App switches", stat { it.switches.toString() }, Modifier.weight(1f))
        }

        if (days > 7) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Android only keeps about a week of detailed history, so older days may be missing.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Export dataset",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        exportOptions.forEachIndexed { index, option ->
            ExportRow(
                option = option,
                selected = index == selectedExport,
                onClick = { if (option.enabled) selectedExport = index }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = if (exporting) "Preparing…" else "Prepare export →",
            onClick = {
                if (!exporting) {
                    if (!granted) {
                        Toast.makeText(context, "Allow usage access first", Toast.LENGTH_SHORT).show()
                    } else if (r == null || r.snapshot.sessions.isEmpty()) {
                        Toast.makeText(
                            context,
                            "No usage data to export for this period yet",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        exporting = true
                        val format = if (selectedExport == 0) ExportFormat.CSV else ExportFormat.JSON
                        scope.launch {
                            try {
                                val file = withContext(Dispatchers.IO) {
                                    createExportFile(context, r, format)
                                }
                                shareExportFile(context, file, format)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Export failed: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } finally {
                                exporting = false
                            }
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "More",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        ReportLinkCard(
            icon = Icons.Rounded.ShowChart,
            color = Color(0xFF6C5CE7),
            title = "Weekly Trends",
            subtitle = "Compare this week with last week",
            onClick = onOpenWeekly
        )
        Spacer(modifier = Modifier.height(12.dp))
        ReportLinkCard(
            icon = Icons.Rounded.Lock,
            color = Color(0xFF12A57F),
            title = "Privacy & Consent",
            subtitle = "Permissions and your data",
            onClick = onOpenPrivacy
        )
        Spacer(modifier = Modifier.height(12.dp))
        ReportLinkCard(
            icon = Icons.Rounded.Inbox,
            color = Color(0xFF3F7BE8),
            title = "Empty state preview",
            subtitle = "Temporary: the \"No Data Yet\" screen",
            onClick = onOpenNoData
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(text = label, fontSize = 15.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
    }
}

@Composable
private fun ExportRow(option: ExportOption, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val borderColor = if (selected) PrimaryPurple else CardBorder
    val alpha = if (option.enabled) 1f else 0.55f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(if (selected) 3.dp else 2.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(option.color.copy(alpha = 0.14f * alpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = option.color.copy(alpha = alpha),
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark.copy(alpha = alpha)
            )
            Text(
                text = option.subtitle,
                fontSize = 14.sp,
                color = TextMuted
            )
        }

        Text(
            text = option.badge,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SuccessGreen
        )
    }
}

@Composable
private fun ReportLinkCard(
    icon: ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(text = subtitle, fontSize = 14.sp, color = TextMuted)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.Black
        )
    }
}