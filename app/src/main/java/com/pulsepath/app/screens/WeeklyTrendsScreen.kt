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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.data.WeeklyData
import com.pulsepath.app.data.formatDuration
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.data.loadWeekly
import com.pulsepath.app.data.percentChange
import com.pulsepath.app.ui.theme.AccentIndigo
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.ChartTeal
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SoftLavender
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.max

private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private val LinePurple = Color(0xFF6D3BFF)
private val WarningOrange = Color(0xFFF59E0B)

@Composable
fun WeeklyTrendsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val granted = hasUsageAccess(context)
    var tab by remember { mutableIntStateOf(0) }

    val weekly by produceState<WeeklyData?>(initialValue = null, granted) {
        value = if (granted) {
            withContext(Dispatchers.IO) { loadWeekly(context) }
        } else {
            null
        }
    }
    val data = weekly

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
                text = "Weekly Trends",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Text(
            text = "Compare your usage and spot patterns",
            fontSize = 17.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Screen Time / App Usage switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf("Screen Time", "App Usage").forEachIndexed { index, label ->
                val selected = index == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) AccentIndigo else SoftLavender)
                        .clickable { tab = index }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else TextDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            !granted -> Text(
                text = "Allow usage access in Settings to compare your weeks.",
                fontSize = 17.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            )

            data == null -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }

            else -> WeeklyContent(data = data, showTime = tab == 0)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WeeklyContent(data: WeeklyData, showTime: Boolean) {
    // Chart values: hours for Screen Time, number of app opens for App Usage
    val thisValues: List<Float?> = if (showTime) {
        data.thisWeekMillis.map { it?.let { millis -> millis / 3_600_000f } }
    } else {
        data.thisWeekOpens.map { it?.toFloat() }
    }
    val lastValues: List<Float> = if (showTime) {
        data.lastWeekMillis.map { it / 3_600_000f }
    } else {
        data.lastWeekOpens.map { it.toFloat() }
    }

    val thisTotal: Long = if (showTime) {
        data.thisWeekMillis.filterNotNull().sum()
    } else {
        data.thisWeekOpens.filterNotNull().sum().toLong()
    }
    val lastToDate: Long = if (showTime) data.lastWeekToDateMillis else data.lastWeekToDateOpens.toLong()
    val days = data.daysSoFar.coerceAtLeast(1)
    val change = percentChange(thisTotal, lastToDate)

    // The top of the chart is a multiple of 5, so the side numbers are whole numbers
    val highest = (thisValues.filterNotNull() + lastValues).maxOrNull() ?: 0f
    val axisMax = max(5, (ceil(highest / 5f) * 5).toInt())

    TrendChartCard(
        thisWeek = thisValues,
        lastWeek = lastValues,
        axisMax = axisMax,
        unit = if (showTime) "h" else ""
    )

    Text(
        text = "This week is compared with the same days last week. Android only keeps about a week of detailed history, so last week may be incomplete.",
        fontSize = 13.sp,
        color = TextMuted,
        lineHeight = 18.sp,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
    )

    Spacer(modifier = Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TrendStatCard(
            title = if (showTime) "Total screen time" else "Total app opens",
            value = if (showTime) formatDuration(thisTotal) else thisTotal.toString(),
            change = change,
            modifier = Modifier.weight(1f)
        )
        TrendStatCard(
            title = if (showTime) "Avg. Daily" else "Avg. per day",
            value = if (showTime) formatDuration(thisTotal / days) else (thisTotal / days).toString(),
            change = change,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Tip banner
    val subject = if (showTime) "Your screen time is" else "You opened apps"
    val bannerText = when {
        change == null -> "Not enough data from last week to compare yet."
        change > 0 && showTime -> "$subject $change% higher than last week. Consider taking more breaks!"
        change > 0 -> "$subject $change% more often than last week. Try to open them less."
        change < 0 && showTime -> "$subject ${-change}% lower than last week. Nice work!"
        change < 0 -> "$subject ${-change}% less often than last week. Nice work!"
        else -> "Your usage is the same as last week."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF8E63F7))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = bannerText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TrendChartCard(
    thisWeek: List<Float?>,
    lastWeek: List<Float>,
    axisMax: Int,
    unit: String
) {
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Legend
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(LinePurple))
            Spacer(modifier = Modifier.width(8.dp))
            Text("This Week", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
            Spacer(modifier = Modifier.width(24.dp))
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(ChartTeal))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Last Week", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(34.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                (0..5).forEach { step ->
                    val value = axisMax * (5 - step) / 5
                    Text(text = "$value$unit", fontSize = 12.sp, color = TextMuted)
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                for (i in 0..5) {
                    val y = size.height * i / 5f
                    drawLine(
                        color = CardBorder,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2f,
                        pathEffect = dash
                    )
                }

                fun pointAt(index: Int, value: Float): Offset {
                    val fraction = (value / axisMax).coerceIn(0f, 1f)
                    return Offset(
                        x = size.width * (index + 0.5f) / dayLabels.size,
                        y = size.height * (1f - fraction)
                    )
                }

                // Future days of this week are null, so they are skipped
                val thisPoints = thisWeek.mapIndexedNotNull { i, v -> v?.let { pointAt(i, it) } }
                val lastPoints = lastWeek.mapIndexed { i, v -> pointAt(i, v) }

                // Last week line
                if (lastPoints.size >= 2) {
                    val lastPath = Path().apply {
                        moveTo(lastPoints.first().x, lastPoints.first().y)
                        addSmoothSegments(lastPoints)
                    }
                    drawPath(
                        path = lastPath,
                        color = ChartTeal,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // This week: soft fill and line
                if (thisPoints.size >= 2) {
                    val areaPath = Path().apply {
                        moveTo(thisPoints.first().x, size.height)
                        lineTo(thisPoints.first().x, thisPoints.first().y)
                        addSmoothSegments(thisPoints)
                        lineTo(thisPoints.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            listOf(LinePurple.copy(alpha = 0.14f), Color.Transparent)
                        )
                    )

                    val thisPath = Path().apply {
                        moveTo(thisPoints.first().x, thisPoints.first().y)
                        addSmoothSegments(thisPoints)
                    }
                    drawPath(
                        path = thisPath,
                        color = LinePurple,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                lastPoints.forEach { drawCircle(ChartTeal, radius = 4.dp.toPx(), center = it) }
                thisPoints.forEach { drawCircle(LinePurple, radius = 4.dp.toPx(), center = it) }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(34.dp))
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Draws smooth curves through the points. Call after moveTo(first point). */
private fun Path.addSmoothSegments(points: List<Offset>) {
    for (i in 1 until points.size) {
        val previous = points[i - 1]
        val current = points[i]
        val midX = (previous.x + current.x) / 2f
        cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
    }
}

@Composable
private fun TrendStatCard(
    title: String,
    value: String,
    change: Int?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)

    val (changeText, changeColor) = when {
        change == null -> "–" to TextMuted
        change > 0 -> "↑ $change%" to WarningOrange
        change < 0 -> "↓ ${-change}%" to SuccessGreen
        else -> "0%" to TextMuted
    }

    Column(
        modifier = modifier
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = value,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = changeText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = changeColor
        )
    }
}