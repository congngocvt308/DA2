package com.cong.smartalarm.feature.alarmedit.smile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm

/** Thư viện âm thanh: thu âm mới, chọn tệp, hoặc dùng lại bản thu trước đó. */
@Composable
fun SoundPickerDialog(
    onDismiss: () -> Unit,
    onRecordNew: () -> Unit,
    onPickFromFile: () -> Unit,
    onSoundSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var filesList by remember {
        mutableStateOf(
            java.io.File(context.filesDir.absolutePath).listFiles { file ->
                file.extension == "m4a" || file.extension == "mp3"
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        CalmCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thư viện âm thanh", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Đóng", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "* Lưu ý: File âm thanh cần dưới 4 giây",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CalmButton(
                        onClick = onRecordNew,
                        modifier = Modifier.weight(0.9f),
                        colors = ButtonDefaults.buttonColors(containerColor = Calm.Destructive, contentColor = Calm.OnDestructive),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Mic, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thu âm", fontSize = 12.sp)
                    }
                    CalmButton(
                        onClick = onPickFromFile,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chọn tệp", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Bản thu trước đó:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    if (filesList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                Text("Chưa có bản thu nào", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                    }
                    items(filesList, key = { it.absolutePath }) { file ->
                        Surface(
                            onClick = { onSoundSelected(file.absolutePath) },
                            shape = RoundedCornerShape(12.dp),
                            color = Calm.Raised,
                            contentColor = Calm.Foreground,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 12.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Audiotrack, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    file.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                IconButton(
                                    onClick = {
                                        if (file.delete()) {
                                            filesList = filesList.filter { it.absolutePath != file.absolutePath }
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa file",
                                        modifier = Modifier.size(18.dp),
                                        tint = Calm.Destructive
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
