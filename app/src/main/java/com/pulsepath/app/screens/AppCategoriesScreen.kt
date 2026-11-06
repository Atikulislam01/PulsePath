package com.pulsepath.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import com.pulsepath.app.components.ArtImage
import com.pulsepath.app.components.BackArrow
import com.pulsepath.app.components.PackageAppIcon
import com.pulsepath.app.data.CATEGORY_EDUCATION
import com.pulsepath.app.data.CATEGORY_GAMING
import com.pulsepath.app.data.CATEGORY_MESSAGING
import com.pulsepath.app.data.CATEGORY_OTHER
import com.pulsepath.app.data.CATEGORY_PRODUCTIVITY
import com.pulsepath.app.data.CATEGORY_SOCIAL
import com.pulsepath.app.data.CATEGORY_VIDEO
import com.pulsepath.app.data.InstalledApp
import com.pulsepath.app.data.loadInstalledApps
import com.pulsepath.app.ui.theme.BackgroundWhite
import com.pulsepath.app.ui.theme.CardBorder
import com.pulsepath.app.ui.theme.NavyHeading
import com.pulsepath.app.ui.theme.PrimaryPurple
import com.pulsepath.app.ui.theme.TextDark
import com.pulsepath.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class CategoryCardData(
    val title: String,        // what the tile says
    val key: String,          // the category name used in the data
    val imageName: String,    // PNG name in res/drawable
    val icon: ImageVector,    // used if the PNG is missing
    val color: Color
)

private val categoryCards = listOf(
    CategoryCardData("Social media", CATEGORY_SOCIAL, "category_social", Icons.Rounded.Forum, Color(0xFFE5484D)),
    CategoryCardData("Messaging", CATEGORY_MESSAGING, "category_messaging", Icons.Rounded.Chat, Color(0xFFF59E0B)),
    CategoryCardData("Games", CATEGORY_GAMING, "category_games", Icons.Rounded.SportsEsports, Color(0xFF9B5DE5)),
    CategoryCardData("Video streaming", CATEGORY_VIDEO, "category_video", Icons.Rounded.LiveTv, Color(0xFF6C5CE7)),
    CategoryCardData("Productivity", CATEGORY_PRODUCTIVITY, "category_productivity", Icons.Rounded.TrendingUp, Color(0xFF0E9F8E)),
    CategoryCardData("Education", CATEGORY_EDUCATION, "category_education", Icons.Rounded.School, Color(0xFF3F7BE8))
)

@Composable
fun AppCategoriesScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    val installed by produceState<List<InstalledApp>?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { loadInstalledApps(context) }
    }
    val apps = installed

    var selected by remember { mutableStateOf<CategoryCardData?>(null) }

    // Pop-up with the apps of the tapped category
    selected?.let { category ->
        val inCategory = apps.orEmpty().filter { it.category == category.key }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(category.title) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (inCategory.isEmpty()) {
                        Text("No apps in this category.", color = TextMuted)
                    }
                    inCategory.forEach { app ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PackageAppIcon(
                                packageName = app.packageName,
                                appName = app.name,
                                size = 36.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = app.name,
                                fontSize = 16.sp,
                                color = TextDark,
                                maxLines = 1
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) {
                    Text("Close", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

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
                text = "App categories",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = screenTitleColor
            )
        }

        Text(
            text = "Organize your apps and set categories.",
            fontSize = 17.sp,
            color = TextDark,
            modifier = Modifier.padding(start = 48.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        categoryCards.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pair.forEach { category ->
                    val countText = when (apps) {
                        null -> "…"
                        else -> {
                            val n = apps.count { it.category == category.key }
                            if (n == 1) "1 app" else "$n apps"
                        }
                    }
                    CategoryTile(
                        category = category,
                        countText = countText,
                        onClick = { selected = category },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (apps != null) {
            val others = apps.count { it.category == CATEGORY_OTHER }
            if (others > 0) {
                Text(
                    text = "$others more apps don't fit any of these categories yet.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CategoryTile(
    category: CategoryCardData,
    countText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = modifier
            .aspectRatio(1.1f)
            .clip(shape)
            .cardBorder(2.dp, CardBorder, shape)
            .pressable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Your Figma icon (category_*.png), or the old round icon if the PNG is missing
        ArtImage(
            name = category.imageName,
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.Start)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Column {
            Text(
                text = category.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = countText,
                fontSize = 15.sp,
                color = TextMuted
            )
        }
    }
}