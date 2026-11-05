package com.pulsepath.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PackageAppIcon
import com.pulsepath.app.data.AppDetailData
import com.pulsepath.app.data.ChartBar
import com.pulsepath.app.data.DetailPeriod
import com.pulsepath.app.data.appLabel
import com.pulsepath.app.data.formatClock
import com.pulsepath.app.data.formatDuration
import com.pulsepath.app.data.formatShortDate
import com.pulsepath.app.data.loadAppDetail
import com.pulsepath.app.ui.theme.AccentIndigo
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.ChartPink
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SoftLavender
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val tabLabels = listOf("Day", "Week", "Month")
private val tabPeriods = listOf(DetailPeriod.DAY, DetailPeriod.WEEK, DetailPeriod.MONTH)

@Composable
fun AppDetailScreen(
    packageName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(1) } // starts on "Week"
    val period = tabPeriods[selectedTab]
    val appName = remember(packageName) { appLabel(context, packageName) }

    // Load on a background thread whenever the app or the tab changes
    val detail by produceState<AppDetailData?>(initialValue = null, packageName, period) {
        value = withContext(Dispatchers.IO) { loadAppDetail(context, packageName, period) }
    }
    val data = detail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        BackArrow(onClick = onBack)

        Spacer(modifier = Modifier.height(8.dp))

        // App icon + name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, CardBorder, RoundedCornerShape(24.dp))
                    .padding(10.dp)
            ) {
                PackageAppIcon(packageName = packageName, appName = appName, size = 72.dp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = appName,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.pulsepath.app.ui.theme.InkBlack,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Usage Details",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Day / Week / Month switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SoftLavender)
                .padding(6.dp)
        ) {
            tabLabels.forEachIndexed { index, label ->
                val selected = index == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) Color.White else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentIndigo
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (data == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            // Total time + launches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = formatDuration(data.totalMillis),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = "Total Usage Time",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = data.launches.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = com.pulsepath.app.ui.theme.InkBlack
                    )
                    Text(
                        text = "Launches",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            UsageChartCard(bars = data.bars, labelIndices = data.labelIndices)

            if (period == DetailPeriod.MONTH) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Android only keeps about a week of detailed history, so older days may look empty.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sessions",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (data.sessions.isEmpty()) {
                Text(
                    text = "No usage recorded for this period.",
                    fontSize = 16.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                )
            }

            data.sessions.forEach { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PackageAppIcon(packageName = packageName, appName = appName, size = 36.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${formatClock(session.startMillis)} - ${formatClock(session.endMillis)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        if (period != DetailPeriod.DAY) {
                            Text(
                                text = formatShortDate(session.startMillis),
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Text(
                        text = formatDuration(session.durationMillis),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UsageChartCard(bars: List<ChartBar>, labelIndices: Set<Int>) {
    val shape = RoundedCornerShape(24.dp)
    val maxMillis = (bars.maxOfOrNull { it.millis } ?: 0L).coerceAtLeast(1L)
    val gap = if (bars.size > 14) 1.dp else 3.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 12.dp, vertical = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            // Dotted grid lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dash = PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
                for (i in 0..3) {
                    val y = size.height * i / 3f
                    drawLine(
                        color = CardBorder,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f,
                        pathEffect = dash
                    )
                }
            }

            // Bars
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { bar ->
                    val fraction = (bar.millis.toFloat() / maxMillis).coerceAtLeast(0.02f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = gap)
                                .fillMaxWidth()
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFF9FD0), ChartPink)
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Labels under some of the bars
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEachIndexed { index, bar ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (index in labelIndices) {
                        Text(
                            text = bar.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            softWrap = false,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                }
            }
        }
    }
}