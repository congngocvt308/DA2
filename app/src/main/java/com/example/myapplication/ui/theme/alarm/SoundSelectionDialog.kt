package com.cong.smartalarm.ui.theme.alarm

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.utils.RingtoneUtils
import com.cong.smartalarm.utils.SoundPlayer

@Composable
fun SoundSelectionDialog(
    currentUri: String,
    currentVolume: Float,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    // Lấy danh sách nhạc 1 lần khi mở Dialog
    val ringtones = remember { RingtoneUtils.getRingtoneList(context) }
    val player = remember { SoundPlayer(context) }

    var selectedUri by remember { mutableStateOf(currentUri) }

    val isCustomFile = remember(selectedUri) {
        ringtones.none { it.uri == selectedUri } && selectedUri.isNotBlank()
    }

    val customFileName = remember(selectedUri) {
        if (isCustomFile) RingtoneUtils.getRingtoneTitle(context, selectedUri) else ""
    }

    DisposableEffect(Unit) {
        onDispose { player.stop() }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(), // Chỉ mở tài liệu
        onResult = { uri ->
            uri?.let {

                // 🚨 LOGIC QUAN TRỌNG NHẤT: XIN QUYỀN LÂU DÀI
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace() // Một số máy cũ có thể không cần hoặc lỗi
                }
                selectedUri = it.toString()
                player.playOrUpdateVolume(it.toString(), currentVolume)
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn nhạc chuông") },
        text = {
            Column {

                Button(
                    onClick = {
                        // Chỉ lọc lấy file âm thanh (mp3, wav...)
                        filePickerLauncher.launch(arrayOf("audio/*"))
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Chọn từ bộ nhớ máy")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- PHẦN HIỂN THỊ FILE TÙY CHỈNH (Nếu đang chọn file ngoài) ---
                if (isCustomFile) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Đang chọn: $customFileName",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Divider(color = Color.Gray, thickness = 0.5.dp)

                // Giới hạn chiều cao để không bị tràn màn hình nếu danh sách quá dài
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(ringtones) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedUri = item.uri
                                    // Nghe thử
                                    player.playOrUpdateVolume(item.uri, currentVolume)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (item.uri == selectedUri),
                                onClick = null, // Xử lý click ở Row
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.title, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedUri)
                onDismiss()
            }) { Text("Xong") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}