package com.pulsepath.app.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulsepath.app.data.loadProfilePhoto
import com.pulsepath.app.data.removeProfilePhoto
import com.pulsepath.app.data.saveProfilePhoto
import com.pulsepath.app.ui.theme.PrimaryPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Round profile picture. Shows the user's own photo if they chose one,
 * otherwise the "user" picture from res/drawable. Tap to change or remove it.
 */
@Composable
fun ProfileAvatar(size: Dp = 88.dp) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var version by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    val photo = remember(version) { loadProfilePhoto(context)?.asImageBitmap() }
    val defaultRes = remember {
        context.resources.getIdentifier("user", "drawable", context.packageName)
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { saveProfilePhoto(context, uri) }
                if (ok) {
                    version++
                } else {
                    Toast.makeText(context, "Couldn't use that picture", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Profile picture") },
            text = { Text("Choose a photo from your gallery. It stays on this phone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Choose photo", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (photo != null) {
                    TextButton(onClick = {
                        showDialog = false
                        removeProfilePhoto(context)
                        version++
                    }) {
                        Text("Remove photo", color = Color(0xFFE5484D))
                    }
                } else {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFE3ECFA))
                .border(2.dp, Color(0xFFDDE3EE), CircleShape)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            when {
                photo != null -> Image(
                    bitmap = photo,
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size)
                )

                defaultRes != 0 -> Image(
                    painter = painterResource(defaultRes),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size)
                )

                else -> Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile picture",
                    tint = Color(0xFF3B5BFF),
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }

        // Small camera badge so people know it can be changed
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(size * 0.32f)
                .clip(CircleShape)
                .background(PrimaryPurple)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.18f)
            )
        }
    }
}