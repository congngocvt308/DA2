package com.cong.smartalarm.feature.alarmedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.theme.Calm


/** Ô nhiệm vụ hiện tại (thêm / sửa / xoá) + 3 ô khoá cho nhiệm vụ sau này. */
@Composable
fun AlarmTaskSection(
    questionCount: Int,
    faceMissionConfig: com.cong.smartalarm.core.database.entity.AlarmFaceMissionConfigEntity?,
    selectedQRCodeIds: List<Int>,
    onAddClick: () -> Unit,
    onClearClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val hasQuiz = questionCount > 0
    val hasSmile = faceMissionConfig != null
    val hasQR = selectedQRCodeIds.isNotEmpty()
    val hasAnyMission = hasQuiz || hasSmile || hasQR
    val tileShape = RoundedCornerShape(18.dp)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. CARD NHIỆM VỤ CHÍNH (80x80) ---
        item {
            Box(modifier = Modifier.padding(top = 8.dp, end = 8.dp)) {
                Column(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(tileShape)
                        .background(if (hasAnyMission) Calm.NeutralMist else Calm.Muted)
                        .border(1.dp, Calm.NeutralSoft, tileShape)
                        .clickable(onClick = if (hasAnyMission) onEditClick else onAddClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (hasAnyMission) Calm.Surface else Calm.NeutralMist),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            hasSmile -> Text("😊", fontSize = 18.sp)
                            hasQuiz -> Text("🧮", fontSize = 18.sp)
                            hasQR -> Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Calm.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            else -> Icon(
                                Icons.Default.Add,
                                contentDescription = "Thêm nhiệm vụ",
                                tint = Calm.Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            hasSmile -> "${faceMissionConfig?.requiredHoldDurationSec} giây"
                            hasQuiz -> "$questionCount câu"
                            hasQR -> "${selectedQRCodeIds.size} mã"
                            else -> "Thêm"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Calm.Foreground,
                        fontSize = 11.sp
                    )
                }

                // NÚT XÓA NHIỆM VỤ (Dấu X đỏ)
                if (hasAnyMission) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .offset(x = 4.dp, y = (-4).dp)
                            .clickable { onClearClick() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa nhiệm vụ",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }

        // --- 2. BA CARD KHÓA (PLACEHOLDERS CHO TƯƠNG LAI) ---
        items(3) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(tileShape)
                    .background(Calm.Muted)
                    .border(1.dp, Calm.Border, tileShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Coming Soon",
                    tint = Calm.MutedForeground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
