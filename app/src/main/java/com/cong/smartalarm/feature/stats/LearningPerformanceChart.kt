package com.cong.smartalarm.feature.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm

/** Biểu đồ cột tỉ lệ trả lời đúng 7 ngày gần nhất, có vạch mục tiêu 80%. */
@Composable
fun LearningPerformanceChart(stats: List<DayAccuracy>) {
    val goal = 0.8f
    val barMax = 112.dp      // chiều cao ứng với 100%
    val labelRoom = 22.dp    // chỗ cho nhãn % phía trên cột cao nhất
    val plotHeight = barMax + labelRoom
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val answered = stats.sumOf { it.answered }
    val correct = stats.sumOf { it.correct }
    val daysWithData = stats.count { it.accuracy != null }
    val daysOnGoal = stats.count { (it.accuracy ?: 0f) >= goal }

    CalmCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            // Tóm tắt: tỉ lệ đúng cả tuần + số ngày đạt mục tiêu
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (answered > 0) "${correct * 100 / answered}%" else "–",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (answered > 0) "trả lời đúng · $answered câu" else "chưa có câu trả lời",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = "Đạt mục tiêu 80%: $daysOnGoal/$daysWithData ngày có luyện tập",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(plotHeight)
                    // Lưới 0 / 50 / 100% và vạch mục tiêu cùng thang với cột
                    .drawBehind {
                        val base = size.height
                        val unit = barMax.toPx()
                        listOf(0f, 0.5f, 1f).forEach { f ->
                            val y = base - unit * f
                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                        }
                        val gy = base - unit * goal
                        drawLine(Calm.Wellness.copy(alpha = 0.45f), Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1.5.dp.toPx())
                    }
            ) {
                Row(Modifier.fillMaxSize()) {
                    stats.forEach { day ->
                        val acc = day.accuracy
                        val reached = acc != null && acc >= goal
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .semantics(mergeDescendants = true) {
                                    contentDescription = if (acc == null) "${day.label}: chưa luyện tập"
                                    else "${day.label}: đúng ${(acc * 100).toInt()}%, ${day.answered} câu"
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = if (acc == null) "–" else "${(acc * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    acc == null -> MaterialTheme.colorScheme.outline
                                    reached -> Calm.WellnessText
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(Modifier.height(4.dp))
                            // Ngày không luyện tập chỉ có chân xám mỏng; 0% có chân đậm hơn để phân biệt
                            val barHeight = if (acc == null) 3.dp else (barMax * acc).coerceAtLeast(5.dp)
                            Box(
                                Modifier
                                    .width(22.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                    .background(
                                        when {
                                            acc == null -> Calm.Muted
                                            reached -> Calm.Wellness
                                            acc == 0f -> Calm.NeutralSoft
                                            else -> Calm.Neutral
                                        }
                                    )
                            )
                        }
                    }
                }
            }

            // Nhãn ngày
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                stats.forEach { day ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = day.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (day.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${day.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Chú thích vạch mục tiêu (đường kẻ xanh trong biểu đồ)
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(16.dp).height(2.dp).background(Calm.Wellness))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Mục tiêu 80% đúng",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (answered == 0) {
                Text(
                    text = "Trả lời câu hỏi khi tắt báo thức để thấy tiến bộ của bạn ở đây.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
