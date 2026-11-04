package com.pulsepath.app.screens

import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.components.ProfileAvatar
import com.pulsepath.app.data.CATEGORY_GAMING
import com.pulsepath.app.data.CATEGORY_MESSAGING
import com.pulsepath.app.data.CATEGORY_PRODUCTIVITY
import com.pulsepath.app.data.CATEGORY_SOCIAL
import com.pulsepath.app.data.CATEGORY_VIDEO
import com.pulsepath.app.data.CategoryTotal
import com.pulsepath.app.data.HomeData
import com.pulsepath.app.data.SettingsPrefs
import com.pulsepath.app.data.UsageRepository
import com.pulsepath.app.data.formatDuration
import com.pulsepath.app.data.hasUsageAccess
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.ChartBlueTop
import com.pulsepath.app.ui.theme.ChartCyanBottom
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.NavySubtitle
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.SuccessGreen
import com.pulsepath.app.ui.theme.SuccessGreenBg
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import kotlin.math.ceil
import kotlin.math.max

private val WarningOrange = Color(0xFFF59E0B)

@Composable
fun HomeScreen(onGrantPermission: () -> Unit) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(hasUsageAccess(context)) }
    var refreshKey by remember { mutableIntStateOf(0) }

    // Every time the user comes back to the app, re-check permission and reload
    val lifecycle = (context as? ComponentActivity)?.lifecycle
    DisposableEffect(lifecycle) {
        if (lifecycle == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    granted = hasUsageAccess(context)
                    refreshKey++
                }
            }
            lifecycle.addObserver(observer)
            onDispose { lifecycle.removeObserver(observer) }
        }
    }

    // Load the real data on a background thread
    val data by produceState<HomeData?>(initialValue = null, granted, refreshKey) {
        value = if (granted) {
            withContext(Dispatchers.IO) { UsageRepository.loadHomeData(context) }
        } else {
            null
        }
    }

    val loaded = data
    when {
        !granted -> NoPermissionState(onGrantPermission)
        loaded == null -> LoadingState()
        else -> HomeContent(loaded)
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PrimaryPurple)
    }
}

@Composable
private fun NoPermissionState(onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Data Yet",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NavyHeading
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Allow usage access to see your screen time and behaviour insights here.",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = NavySubtitle,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(text = "Start Monitoring", onClick = onGrantPermission)
    }
}

@Composable
private fun HomeContent(data: HomeData) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(SettingsPrefs.userName(context)) }
    // Ask for the name once, the first time Home is shown
    var showNameDialog by remember { mutableStateOf(!SettingsPrefs.wasNameAsked(context)) }

    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    if (showNameDialog) {
        NameDialog(
            currentName = name,
            onDismiss = {
                SettingsPrefs.setNameAsked(context)
                showNameDialog = false
            },
            onSave = { newName ->
                SettingsPrefs.setUserName(context, newName)
                SettingsPrefs.setNameAsked(context)
                name = newName.trim()
                showNameDialog = false
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
        Spacer(modifier = Modifier.height(16.dp))

        // Greeting + profile picture
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (name.isBlank()) "$greeting!" else "$greeting,\n$name!",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyHeading,
                lineHeight = 34.sp,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showNameDialog = true }   // tap the greeting to change your name
            )
            Spacer(modifier = Modifier.width(12.dp))
            ProfileAvatar(size = 88.dp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        ScreenTimeCard(todayMillis = data.todayMillis, changePercent = data.changePercent)

        Spacer(modifier = Modifier.height(20.dp))

        WeeklyBarChartCard(values = data.weekMillis, labels = data.weekLabels)

        Spacer(modifier = Modifier.height(24.dp))

        // Top category header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Top Category",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "By screen time",
                    fontSize = 14.sp,
                    color = TextMuted
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = TextDark
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (data.categories.isEmpty()) {
            Text(
                text = "No app usage recorded yet today. Use your phone a bit, then come back.",
                fontSize = 16.sp,
                color = TextMuted,
                lineHeight = 22.sp
            )
        } else {
            val topMillis = data.categories.first().millis.toFloat()
            data.categories.forEach { category ->
                CategoryRow(category = category, fraction = category.millis / topMillis)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun NameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What's your name?") },
        text = {
            Column {
                Text(
                    text = "PulsePath uses it in the greeting on Home. It stays on this phone. You can change it later by tapping the greeting.",
                    fontSize = 14.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 20) text = it },
                    singleLine = true,
                    placeholder = { Text("Your name") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Save", color = PrimaryPurple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        }
    )
}

@Composable
private fun ScreenTimeCard(todayMillis: Long, changePercent: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(2.dp, CardBorder, RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Text(
            text = "TODAY'S SCREEN TIME",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formatDuration(todayMillis),
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

        // Only show the pill when we have last week's data to compare with
        if (changePercent != null) {
            Spacer(modifier = Modifier.height(12.dp))

            val isHigher = changePercent > 0
            val pillColor = if (isHigher) WarningOrange else SuccessGreen
            val pillBackground = if (isHigher) WarningOrange.copy(alpha = 0.15f) else SuccessGreenBg
            val pillText = when {
                changePercent < 0 -> "↓ ${-changePercent}% vs last week"
                changePercent > 0 -> "↑ $changePercent% vs last week"
                else -> "Same as last week"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(pillBackground)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = pillText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = pillColor
                )
            }
        }
    }
}

@Composable
private fun WeeklyBarChartCard(values: List<Long>, labels: List<String>) {
    // The top of the chart is a multiple of 5 hours, so the side numbers are whole hours
    val maxHours = (values.maxOrNull() ?: 0L) / 3_600_000.0
    val axisMaxHours = max(5, (ceil(maxHours / 5.0) * 5).toInt())
    val axisMaxMillis = axisMaxHours * 3_600_000f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .border(2.dp, CardBorder, RoundedCornerShape(28.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            // Y-axis numbers
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                (0..5).forEach { step ->
                    val hours = axisMaxHours * (5 - step) / 5
                    Text(text = "${hours}h", fontSize = 10.sp, color = TextMuted)
                }
            }

            // Grid lines + bars
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
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
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    values.forEach { millis ->
                        val fraction = (millis / axisMaxMillis).coerceIn(0.01f, 1f)
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .fillMaxHeight(fraction)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(ChartBlueTop, ChartCyanBottom)
                                    )
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day labels
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(32.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                labels.forEach { label ->
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(28.dp)
                    )
                }
            }
        }
    }
}

private fun categoryStyle(name: String): Pair<Color, ImageVector> = when (name) {
    CATEGORY_SOCIAL -> Color(0xFF6C5CE7) to Icons.Rounded.Forum
    CATEGORY_MESSAGING -> Color(0xFFF59E0B) to Icons.Rounded.Chat
    CATEGORY_VIDEO -> Color(0xFF3F7BE8) to Icons.Rounded.Movie
    CATEGORY_GAMING -> Color(0xFF0E9F8E) to Icons.Rounded.SportsEsports
    CATEGORY_PRODUCTIVITY -> Color(0xFF12B76A) to Icons.Rounded.TrendingUp
    else -> Color(0xFF6B7A99) to Icons.Rounded.GridView
}

@Composable
private fun CategoryRow(category: CategoryTotal, fraction: Float) {
    val (color, icon) = categoryStyle(category.name)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Text(
                    text = formatDuration(category.millis),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}