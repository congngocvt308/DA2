package com.cong.smartalarm.feature.alarmedit.smile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.core.designsystem.theme.rememberReducedMotion

/** Hộp thoại thu âm giọng nói cho một mục âm thanh (tối đa 4 giây). */
@Composable
internal fun VoiceRecordingDialog(
    label: String,
    isRecording: Boolean,
    onDismiss: () -> Unit,
    onStart: () -> Unit,
    onStopAndSave: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isRecording) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isRecording,
            dismissOnClickOutside = !isRecording
        )
    ) {
        // Nhịp "thở" chậm báo đang thu âm (tắt khi giảm chuyển động)
        val reducedMotion = rememberReducedMotion()
        val infiniteTransition = rememberInfiniteTransition(label = "recording")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        CalmCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.width(280.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRecording) "Đang thu âm..." else "Sẵn sàng thu âm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Mục: $label",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Calm.DestructiveMist else Calm.NeutralMist)
                        .border(2.dp, if (isRecording) Calm.Destructive else Calm.Neutral, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isRecording) "Đang thu âm" else "Micro",
                        tint = if (isRecording) Calm.Destructive else Calm.Primary,
                        modifier = Modifier.size(40.dp).graphicsLayer { val s = if (isRecording && !reducedMotion) scale else 1f; scaleX = s; scaleY = s }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (isRecording) {
                    CalmButton(
                        onClick = onStopAndSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Calm.Destructive, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Stop, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dừng & Lưu")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Hủy")
                        }
                        CalmButton(
                            onClick = onStart,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp) // Xóa padding mặc định của button nếu muốn sát hơn
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 17.dp), // Tùy chỉnh độ lệch sang trái ở đây
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bắt đầu", maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }
}
