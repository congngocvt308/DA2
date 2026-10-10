package com.cong.smartalarm.feature.alarmedit.smile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm

/** Thẻ "Âm thanh": chọn Giọng AI / Tùy chỉnh và 4 dòng âm thanh riêng. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SmileSoundSection(
    soundMode: String,
    isDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    onSoundModeChange: (String) -> Unit,
    reminderSoundUri: String?,
    eyeOpenSoundUri: String?,
    smileSoundUri: String?,
    holdPoseSoundUri: String?,
    currentPlayingLabel: String?,
    onPlay: (label: String, uri: String?) -> Unit,
    onBrowse: (label: String) -> Unit,
    onReset: (label: String) -> Unit
) {
    CalmCard(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Calm.Border),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Âm thanh:")
                }

                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { onDropdownExpandedChange(!isDropdownExpanded) }
                ) {
                    // Nút anchor thiết kế lại cho hiện đại hơn
                    Surface(
                        modifier = Modifier
                            .menuAnchor()
                            .width(140.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Calm.NeutralMist,
                        contentColor = Calm.Foreground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Calm.NeutralSoft)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = soundMode, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Icon(
                                imageVector = if (isDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { onDropdownExpandedChange(false) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(text = { Text("Giọng AI", fontSize = 14.sp) }, onClick = { onSoundModeChange("Giọng AI"); onDropdownExpandedChange(false) })
                        DropdownMenuItem(text = { Text("Tùy chỉnh", fontSize = 14.sp) }, onClick = { onSoundModeChange("Tùy chỉnh"); onDropdownExpandedChange(false) })
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = soundMode == "Tùy chỉnh",
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Đường kẻ phân cách
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Thay đổi nhãn thành "Nhắc nhở" như thảo luận trước
                    SoundConfigRow(
                        icon = Icons.Default.Notifications,
                        label = "(Nhắc nhở)",
                        hasFile = reminderSoundUri != null,
                        isPlaying = currentPlayingLabel == "Nhắc nhở",
                        onPlayClick = { onPlay("Nhắc nhở", reminderSoundUri) },
                        onBrowseClick = { onBrowse("Nhắc nhở") },
                        onResetClick = { onReset("Nhắc nhở") }
                    )
                    SoundConfigRow(
                        icon = Icons.Default.Visibility,
                        label = "(Mở mắt)",
                        hasFile = eyeOpenSoundUri != null,
                        isPlaying = currentPlayingLabel == "Mở mắt",
                        onPlayClick = { onPlay("Mở mắt", eyeOpenSoundUri) },
                        onBrowseClick = { onBrowse("Mở mắt") },
                        onResetClick = { onReset("Mở mắt") }
                    )
                    SoundConfigRow(
                        icon = Icons.Default.SentimentSatisfiedAlt,
                        label = "(Cười lên)",
                        hasFile = smileSoundUri != null,
                        isPlaying = currentPlayingLabel == "Cười lên",
                        onPlayClick = { onPlay("Cười lên", smileSoundUri) },
                        onBrowseClick = { onBrowse("Cười lên") },
                        onResetClick = { onReset("Cười lên") }
                    )
                    SoundConfigRow(
                        icon = Icons.Default.HourglassEmpty,
                        label = "(Giữ nguyên)",
                        hasFile = holdPoseSoundUri != null,
                        isPlaying = currentPlayingLabel == "Giữ nguyên",
                        onPlayClick = { onPlay("Giữ nguyên", holdPoseSoundUri) },
                        onBrowseClick = { onBrowse("Giữ nguyên") },
                        onResetClick = { onReset("Giữ nguyên") }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundConfigRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    hasFile: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onBrowseClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row {
            IconButton(onClick = onPlayClick, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = (if (isPlaying) "Dừng" else "Nghe thử") + if (hasFile) " (âm thanh riêng)" else " (giọng AI)",
                    modifier = Modifier.size(20.dp),
                    tint = if (hasFile) Calm.Wellness else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onBrowseClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.FolderOpen, "Chọn âm thanh", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onResetClick, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Refresh, "Đặt lại", modifier = Modifier.size(20.dp), tint = Calm.Destructive)
            }
        }
    }
}
