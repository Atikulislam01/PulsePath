package com.pulsepath.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PackageAppIcon
import com.pulsepath.app.data.AppPairCount
import com.pulsepath.app.data.SwitchingData
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.data.loadSwitching
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.CyanAccent
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val WarningOrange = Color(0xFFF59E0B)

@Composable
fun AppSwitchingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val granted = hasUsageAccess(context)
    val cardShape = RoundedCornerShape(28.dp)

    val switching by produceState<SwitchingData?>(initialValue = null, granted) {
        value = if (granted) {
            withContext(Dispatchers.IO) { loadSwitching(context) }
        } else {
            null
        }
    }
    val data = switching

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
                text = "App switching",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Text(
            text = "See how often you switch between apps and which app pairs are most common.",
            fontSize = 17.sp,
            color = TextDark,
            lineHeight = 24.sp,
            modifier = Modifier.padding(start = 48.dp, end = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            !granted -> Text(
                text = "Allow usage access in Settings to see how you switch between apps.",
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

            else -> {
                // Switching frequency card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .border(2.dp, CardBorder, cardShape)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Switching Frequency",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = data.todaySwitches.toString(),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark
                            )
                            Text(
                                text = " today",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextDark,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        val change = data.changePercent
                        if (change != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val text = when {
                                change > 0 -> "↑ $change% vs. yesterday"
                                change < 0 -> "↓ ${-change}% vs. yesterday"
                                else -> "Same as yesterday"
                            }
                            Text(
                                text = text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    change > 0 -> WarningOrange
                                    change < 0 -> SuccessGreen
                                    else -> TextMuted
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val peak = (data.dailySwitches.maxOrNull() ?: 0).coerceAtLeast(1)
                    SwitchingLineChart(
                        points = data.dailySwitches.map { it.toFloat() / peak },
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    )
                }

                Text(
                    text = "The chart shows your switches over the last 7 days.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Top app pairs card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .border(2.dp, CardBorder, cardShape)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Top App Pairs",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.pulsepath.app.ui.theme.InkBlack
                        )
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = CyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (data.pairs.isEmpty()) {
                        Text(
                            text = "No app switches recorded in the last 7 days.",
                            fontSize = 16.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        data.pairs.forEach { pair -> PairRow(pair) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PairRow(pair: AppPairCount) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PackageAppIcon(packageName = pair.firstPackage, appName = pair.firstName, size = 44.dp)
        Spacer(modifier = Modifier.width(8.dp))
        PackageAppIcon(packageName = pair.secondPackage, appName = pair.secondName, size = 44.dp)

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "${pair.firstName} + ${pair.secondName}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = com.pulsepath.app.ui.theme.InkBlack,
            maxLines = 2,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (pair.times == 1) "1 time" else "${pair.times} times",
            fontSize = 16.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun SwitchingLineChart(points: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = Color(0xFF0B4DFF)

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val padX = 8.dp.toPx()
        val padY = 8.dp.toPx()
        val stepX = (size.width - 2 * padX) / (points.size - 1)
        val usableHeight = size.height - 2 * padY

        fun xAt(i: Int) = padX + i * stepX
        fun yAt(v: Float) = padY + usableHeight * (1f - v)

        // Soft filled area under the line
        val fillPath = Path().apply {
            moveTo(xAt(0), size.height)
            points.forEachIndexed { i, v -> lineTo(xAt(i), yAt(v)) }
            lineTo(xAt(points.lastIndex), size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(lineColor.copy(alpha = 0.18f), Color.Transparent)
            )
        )

        // The line itself
        val linePath = Path().apply {
            moveTo(xAt(0), yAt(points[0]))
            for (i in 1 until points.size) {
                lineTo(xAt(i), yAt(points[i]))
            }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dots
        points.forEachIndexed { i, v ->
            drawCircle(
                color = lineColor,
                radius = 5.dp.toPx(),
                center = Offset(xAt(i), yAt(v))
            )
        }
    }
}