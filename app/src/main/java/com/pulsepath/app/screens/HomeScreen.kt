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
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.pulsepath.app.components.ArtImage
import com.pulsepath.app.components.PackageAppIcon
import com.pulsepath.app.components.PrimaryButton
import com.pulsepath.app.components.ProfileAvatar
import com.pulsepath.app.data.AppUsageSummary
import com.pulsepath.app.data.CATEGORY_EDUCATION
import com.pulsepath.app.data.CATEGORY_GAMING
import com.pulsepath.app.data.CATEGORY_MESSAGING
import com.pulsepath.app.data.CATEGORY_PRODUCTIVITY
import com.pulsepath.app.data.CATEGORY_SOCIAL
import com.pulsepath.app.data.CATEGORY_VIDEO
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.ceil
import kotlin.math.max

private val WarningOrange = Color(0xFFF59E0B)

/** The greeting for an hour of the day (0 to 23). */
private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..20 -> "Good evening"
    else -> "Good night"
}

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
        else -> HomeContent(loaded, refreshKey)
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
private fun HomeContent(data: HomeData, refreshKey: Int) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(SettingsPrefs.userName(context)) }
    // Ask for the name once, the first time Home is shown
    var showNameDialog by remember { mutableStateOf(!SettingsPrefs.wasNameAsked(context)) }

    // The current hour, refreshed every minute and whenever the app is opened again
    var hour by remember { mutableIntStateOf(LocalTime.now().hour) }
    LaunchedEffect(refreshKey) {
        hour = LocalTime.now().hour
        while (true) {
            delay(60_000L)
            hour = LocalTime.now().hour
        }
    }
    val greeting = greetingForHour(hour)

    // Today's apps, used when a category is opened
    val todayApps by produceState<List<AppUsageSummary>?>(
        initialValue = null,
        data.todayMillis,
        refreshKey
    ) {
        value = withContext(Dispatchers.IO) {
            UsageRepository.loadDay(context, LocalDate.now()).apps
        }
    }

    // Which category is open (only one at a time)
    var openCategory by remember { mutableStateOf<String?>(null) }

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

        Text(
            text = "Top Category",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark
        )

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
                val isOpen = openCategory == category.name

                CategoryRow(
                    name = category.name,
                    millis = category.millis,
                    fraction = category.millis / topMillis,
                    isOpen = isOpen,
                    onClick = { openCategory = if (isOpen) null else category.name }
                )

                // The apps inside this category
                if (isOpen) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryAppList(
                        apps = todayApps?.filter { it.category == category.name }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** The apps of one category with their sessions and total time. null means still loading. */
@Composable
private fun CategoryAppList(apps: List<AppUsageSummary>?) {
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        when {
            apps == null -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }

            apps.isEmpty() -> Text(
                text = "No apps used in this category today.",
                fontSize = 15.sp,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            else -> apps.forEachIndexed { index, app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PackageAppIcon(
                        packageName = app.packageName,
                        appName = app.appName,
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark,
                            maxLines = 1
                        )
                        Text(
                            text = if (app.launches == 1) "1 session" else "${app.launches} sessions",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = formatDuration(app.totalMillis),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
                if (index < apps.lastIndex) {
                    HorizontalDivider(color = CardBorder)
                }
            }
        }
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
    CATEGORY_EDUCATION -> Color(0xFF3F7BE8) to Icons.Rounded.School
    else -> Color(0xFF6B7A99) to Icons.Rounded.GridView
}

/** The name of the Figma PNG in res/drawable for each category. */
private fun categoryImageName(name: String): String = when (name) {
    CATEGORY_SOCIAL -> "category_social"
    CATEGORY_MESSAGING -> "category_messaging"
    CATEGORY_VIDEO -> "category_video"
    CATEGORY_GAMING -> "category_games"
    CATEGORY_PRODUCTIVITY -> "category_productivity"
    CATEGORY_EDUCATION -> "category_education"
    else -> "category_other"
}

@Composable
private fun CategoryRow(
    name: String,
    millis: Long,
    fraction: Float,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    val (color, icon) = categoryStyle(name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Your Figma icon, or the old round icon if the PNG is missing
        ArtImage(
            name = categoryImageName(name),
            modifier = Modifier.size(48.dp)
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
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDuration(millis),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Icon(
                        imageVector = if (isOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (isOpen) "Hide apps" else "Show apps",
                        tint = TextMuted
                    )
                }
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