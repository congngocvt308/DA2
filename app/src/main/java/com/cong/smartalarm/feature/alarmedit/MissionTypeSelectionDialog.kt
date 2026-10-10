package com.cong.smartalarm.feature.alarmedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cong.smartalarm.core.designsystem.component.CalmDialogSurface
import com.cong.smartalarm.core.designsystem.theme.Calm


/** Hộp thoại chọn loại nhiệm vụ: trắc nghiệm, nụ cười hoặc quét mã. */
@Composable
fun MissionTypeSelectionDialog(
    onDismiss: () -> Unit,
    onQuizSelected: () -> Unit,
    onSmileChallengeSelected: () -> Unit,
    onQRCodeSelected: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CalmDialogSurface(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chọn loại nhiệm vụ",
                            color = Calm.Foreground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hoàn thành nhiệm vụ để tắt chuông",
                            color = Calm.MutedForeground,
                            fontSize = 13.sp
                        )
                    }
                    // Nút đóng tròn nhỏ
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Calm.NeutralMist)
                            .border(1.dp, Calm.NeutralSoft, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Calm.Foreground, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                MissionTypeItem(
                    title = "Giải trắc nghiệm",
                    description = "Giải các câu hỏi để tỉnh táo.",
                    icon = "🧮",
                    accent = listOf(Calm.NeutralMist, Calm.NeutralMist),
                    onClick = onQuizSelected
                )
                Spacer(modifier = Modifier.height(10.dp))
                MissionTypeItem(
                    title = "Thử thách nụ cười",
                    description = "Mở mắt, cười thật tươi và giữ tư thế để xác nhận thức dậy.",
                    icon = "😊",
                    accent = listOf(Calm.WarmMist, Calm.WarmMist),
                    onClick = onSmileChallengeSelected
                )
                Spacer(modifier = Modifier.height(10.dp))
                MissionTypeItem(
                    title = "Quét mã QR/Barcode",
                    description = "Rời khỏi giường và quét mã để tắt chuông.",
                    icon = Icons.Default.QrCodeScanner,
                    accent = listOf(Calm.WellnessMist, Calm.WellnessMist),
                    onClick = onQRCodeSelected
                )
            }
        }
    }
}

@Composable
private fun MissionTypeItem(
    title: String,
    description: String,
    icon: Any,
    accent: List<Color>,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Calm.Surface)
            .border(1.dp, Calm.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon trong "giọt" gradient màu riêng của từng nhiệm vụ
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(accent)),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is String -> Text(text = icon, fontSize = 22.sp)
                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Calm.WellnessText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Calm.Foreground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Calm.MutedForeground
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Calm.MutedForeground
        )
    }
}
