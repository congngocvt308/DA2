package com.cong.smartalarm.feature.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.database.model.SrsStat
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm

/** Thẻ phân phối SRS: thanh xếp chồng Đã thuộc / Đang học / Mới, bấm để xem danh sách. */
@Composable
fun SrsDistributionSection(
    srsStats: List<SrsStat>,
    onStatusClick: (String) -> Unit
) {
    // 1. Tính tổng để chia tỷ lệ
    val totalQuestions = srsStats.sumOf { it.count }.coerceAtLeast(1)
    val mastered = srsStats.find { it.status == "Mastered" }?.count ?: 0
    val learning = srsStats.find { it.status == "Learning" }?.count ?: 0
    val new = srsStats.find { it.status == "New" }?.count ?: 0
    // Thang màu dịu: mới (nhạt) -> đang học (lavender) -> đã thuộc (wellness)
    val srsMastered = Calm.Wellness
    val srsLearning = Calm.Neutral
    val srsNew = Calm.NeutralSoft

    CalmCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tiêu đề tổng quan
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Phân phối kiến thức", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text("$totalQuestions câu hỏi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            // 2. Thanh Stacked Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Calm.Muted),
                // Khe 2dp tách các đoạn, không chỉ dựa vào màu
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (totalQuestions > 0) {
                    Box(
                        modifier = Modifier
                            .weight((mastered.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(srsMastered)
                            .semantics { contentDescription = "Đã thuộc: $mastered câu"; role = Role.Button }
                            .clickable { onStatusClick("Mastered") }
                    )
                    Box(
                        modifier = Modifier
                            .weight((learning.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(srsLearning)
                            .semantics { contentDescription = "Đang học: $learning câu"; role = Role.Button }
                            .clickable { onStatusClick("Learning") }
                    )
                    Box(
                        modifier = Modifier
                            .weight((new.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(srsNew)
                            .semantics { contentDescription = "Mới: $new câu"; role = Role.Button }
                            .clickable { onStatusClick("New") }
                    )
                }
            }

            // 3. Chú thích
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Chú thích cũng bấm được, vùng chạm >= 48dp
                Box(Modifier.heightIn(min = 48.dp).clickable { onStatusClick("Mastered") }, contentAlignment = Alignment.CenterStart) {
                    LegendItem(color = srsMastered, label = "Đã thuộc ($mastered)")
                }
                Box(Modifier.heightIn(min = 48.dp).clickable { onStatusClick("Learning") }, contentAlignment = Alignment.CenterStart) {
                    LegendItem(color = srsLearning, label = "Đang học ($learning)")
                }
                Box(Modifier.heightIn(min = 48.dp).clickable { onStatusClick("New") }, contentAlignment = Alignment.CenterStart) {
                    LegendItem(color = srsNew, label = "Mới ($new)")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
