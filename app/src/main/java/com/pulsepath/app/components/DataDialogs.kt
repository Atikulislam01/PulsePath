package com.pulsepath.app.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.pulsepath.app.ui.theme.PrimaryPurple

@Composable
fun DeleteDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete saved history?") },
        text = {
            Text(
                "This deletes the usage history PulsePath saved on this phone. " +
                        "You can restore it for 30 days from Settings → Data recovery.\n\n" +
                        "Android keeps its own recent usage data, and PulsePath can't delete that. " +
                        "While Monitoring is on, the last 7 days are saved again the next time you open the app."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = Color(0xFFE5484D), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecoverDataDialog(
    dayCount: Int,
    daysLeft: Int,
    onDismiss: () -> Unit,
    onRestore: () -> Unit
) {
    if (dayCount <= 0) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Data recovery") },
            text = {
                Text(
                    "There is no deleted data to restore. After you delete your history " +
                            "you can bring it back here for 30 days."
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Restore deleted history?") },
            text = {
                Text(
                    "$dayCount days of deleted history can be restored. " +
                            "You have $daysLeft days left before it is erased for good."
                )
            },
            confirmButton = {
                TextButton(onClick = onRestore) {
                    Text("Restore", color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }
}