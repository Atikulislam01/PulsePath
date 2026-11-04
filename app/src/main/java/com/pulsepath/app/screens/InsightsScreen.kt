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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsepath.app.components.ArtImage
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PackageAppIcon
import com.pulsepath.app.data.InsightsData
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.data.loadInsights
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private val WarningOrange = Color(0xFFF59E0B)

@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    onOpenSwitching: () -> Unit,
    onOpenCategories: () -> Unit
) {
    val context = LocalContext.current
    val granted = hasUsageAccess(context)
    val bold = SpanStyle(fontWeight = FontWeight.ExtraBold)
    val cardShape = RoundedCornerShape(28.dp)

    val insights by produceState<InsightsData?>(initialValue = null, granted) {
        value = if (granted) {
            withContext(Dispatchers.IO) { loadInsights(context) }
        } else {
            null
        }
    }
    val data = insights

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
                text = "Behaviour Insights",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading
            )
        }

        Text(
            text = "Discover patterns and improve your digital wellbeing",
            fontSize = 17.sp,
            color = TextDark,
            lineHeight = 24.sp,
            modifier = Modifier.padding(start = 48.dp, end = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Key Insights card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .border(2.dp, CardBorder, cardShape)
                .padding(24.dp)
        ) {
            Text(
                text = "Key Insights",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                !granted -> Text(
                    text = "Allow usage access to see your insights here.",
                    fontSize = 17.sp,
                    color = TextMuted,
                    lineHeight = 24.sp
                )

                data == null -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }

                else -> {
                    InsightRow(
                        leading = { IconBadge(Icons.Rounded.Forum, Color(0xFF3F7BE8)) },
                        text = socialText(data, bold)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    InsightRow(
                        leading = {
                            if (data.recentPackage != null) {
                                PackageAppIcon(
                                    packageName = data.recentPackage,
                                    appName = data.recentAppName ?: "",
                                    size = 40.dp
                                )
                            } else {
                                IconBadge(Icons.Rounded.Schedule, Color(0xFFFF8A00))
                            }
                        },
                        text = recentText(data, bold)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    InsightRow(
                        leading = { IconBadge(Icons.Rounded.Timer, Color(0xFFE5484D)) },
                        text = sessionText(data, bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        NavCard(
            icon = Icons.Rounded.SwapHoriz,
            color = Color(0xFF3F7BE8),
            title = "App switching",
            subtitle = "See which apps you jump between",
            onClick = onOpenSwitching
        )

        Spacer(modifier = Modifier.height(12.dp))

        NavCard(
            icon = Icons.Rounded.GridView,
            color = Color(0xFF9B5DE5),
            title = "App categories",
            subtitle = "Organize your apps",
            onClick = onOpenCategories
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Your insights_illustration.png, or the placeholder
        val artModifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
        ArtImage(name = "insights_illustration", modifier = artModifier) {
            WellbeingIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------- Insight sentences ----------

private fun socialText(d: InsightsData, bold: SpanStyle): AnnotatedString = buildAnnotatedString {
    val evening = d.eveningSocialMillis
    val day = d.daySocialMillis

    when {
        evening == 0L && day == 0L ->
            append("No social media use recorded in the last 7 days.")

        day == 0L -> {
            append("You use social media ")
            withStyle(bold) { append("only in the evening") }
            append(".")
        }

        evening == 0L -> {
            append("You use social media ")
            withStyle(bold) { append("only during the day") }
            append(".")
        }

        else -> {
            // Compare per hour: the evening has 6 hours, the daytime has 12
            val ratio = 2.0 * evening / day
            when {
                ratio >= 1.2 -> {
                    append("You use social media ")
                    withStyle(bold) { append(String.format(Locale.ENGLISH, "%.1f× ", ratio)) }
                    append("more in the evening.")
                }
                ratio <= 1.0 / 1.2 -> {
                    append("You use social media ")
                    withStyle(bold) { append(String.format(Locale.ENGLISH, "%.1f× ", 1.0 / ratio)) }
                    append("more during the day.")
                }
                else -> append("Your social media use is spread evenly through the day.")
            }
        }
    }
}

private fun recentText(d: InsightsData, bold: SpanStyle): AnnotatedString = buildAnnotatedString {
    val name = d.recentAppName
    if (name == null) {
        append("No apps opened in the last 2 hours.")
    } else {
        val times = if (d.recentLaunches == 1) "1 time" else "${d.recentLaunches} times"
        append("You opened ")
        withStyle(bold) { append("$name $times ") }
        append("in the last 2 hours.")
    }
}

private fun sessionText(d: InsightsData, bold: SpanStyle): AnnotatedString = buildAnnotatedString {
    val avg = d.avgSessionMillis
    if (avg == null) {
        append("No app sessions recorded today yet.")
    } else {
        val minutes = avg / 60_000L
        append("Your average session today is ")
        withStyle(bold) { append(if (minutes < 1) "under a minute " else "$minutes min ") }

        val change = d.avgChangePercent
        if (change != null) {
            val color = if (change > 0) WarningOrange else SuccessGreen
            val label = when {
                change > 0 -> "(↑ $change% vs yesterday)"
                change < 0 -> "(↓ ${-change}% vs yesterday)"
                else -> "(same as yesterday)"
            }
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(label) }
        }
    }
}

// ---------- Pieces ----------

@Composable
private fun NavCard(
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, color)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TextMuted
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.Black
        )
    }
}

@Composable
private fun InsightRow(
    leading: @Composable () -> Unit,
    text: AnnotatedString
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        leading()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun WellbeingIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF4F7FC)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFFDFF3F8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SelfImprovement,
                contentDescription = null,
                tint = PrimaryPurple,
                modifier = Modifier.size(110.dp)
            )
        }

        Bubble(Icons.Rounded.Schedule, Color(0xFFFFE2B8), Color(0xFFFF8A00), 64.dp, (-100).dp, (-60).dp)
        Bubble(Icons.Rounded.Timer, Color(0xFFD6CCFF), Color(0xFF3B2FD1), 64.dp, 100.dp, (-60).dp)
        Bubble(Icons.Rounded.Check, Color(0xFF12B76A), Color.White, 56.dp, 110.dp, 60.dp)
    }
}

@Composable
private fun Bubble(
    icon: ImageVector,
    background: Color,
    tint: Color,
    size: Dp,
    x: Dp,
    y: Dp
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}