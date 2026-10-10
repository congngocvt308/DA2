package com.cong.smartalarm.feature.stats


import com.cong.smartalarm.core.designsystem.component.CalmCard

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Collections // 🔥 Icon cho Bảo tàng ký ức
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.core.database.model.SrsStat
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.feature.museum.MemoryMuseumScreen // 🔥 Import màn hình Bảo tàng ký ức

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onNavigateToMuseum: () -> Unit) {
    val viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)

    val score by viewModel.wakeUpScore.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyAccuracy.collectAsStateWithLifecycle(initialValue = emptyList())
    val srsStats by viewModel.srsDistribution.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val questions by viewModel.filteredQuestions.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()

    // 🔥 STATE ĐỂ CHUYỂN SANG MÀN HÌNH BẢO TÀNG KÝ ỨC
    var showMemoryMuseum by remember { mutableStateOf(false) }

    // Nếu người dùng click vào Bảo tàng ký ức -> Hiển thị MemoryMuseumScreen
    if (showMemoryMuseum) {
        MemoryMuseumScreen(
            onBackClick = { showMemoryMuseum = false }
        )
        return // Dừng vẽ StatsScreen để hiển thị đè màn hình Bảo tàng ký ức
    }

    if (selectedStatus != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectSrsStatus(null) },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
                Text(
                    text = "Danh sách câu hỏi: $selectedStatus",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    items(questions, key = { it.questionId }) { question ->
                        ListItem(
                            headlineContent = { Text(question.prompt) },
                            supportingContent = { Text("ID: ${question.questionId}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }

    // Cập nhật điểm tỉnh táo khi mở màn hình
    LaunchedEffect(Unit) {
        viewModel.calculateWakeUpPerformance()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 10.dp)
    ){
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp), // Tăng bottom để ko bị che bởi Nav mới
            verticalArrangement = Arrangement.spacedBy(8.dp) // Giảm khoảng cách giữa các card
        ) {
            item {
                userStats?.let { stats ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallStatCard(
                            label = "Chuỗi ngày",
                            value = "${stats.currentStreak} 🔥",
                            modifier = Modifier.weight(1f)
                        )
                        SmallStatCard(
                            label = "Tổng điểm",
                            value = "${stats.totalPoints} ⭐",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 1. Hiển thị Chỉ số tỉnh táo (Wake-up Score)
            item {
                StatCard(
                    title = "Chỉ số tỉnh táo",
                    value = "${score.toInt()}/100",
                    subValue = if (score > 80) "Rất kỷ luật!" else "Cần cố gắng dậy sớm hơn",
                    icon = Icons.Default.Bedtime,
                    color = Calm.WellnessText // xanh đậm để chữ phụ đạt 4.5:1
                )
            }

            // 🔥 2. MỤC "BẢO TÀNG KÝ ỨC" (CLICK VÀO MỞ MÀN HÌNH BẢO TÀNG)
            item {
                MemoryMuseumCard(
                    onClick = onNavigateToMuseum
                )
            }

            // 3. Hiển thị Trạng thái trí nhớ (SRS)
            item {
                Text(
                    text = "Trạng thái trí nhớ (SRS)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
                )
                SrsDistributionSection(srsStats) { status ->
                    viewModel.selectSrsStatus(status)
                }
            }

            // 4. Hiển thị Hiệu suất học tập 7 ngày qua
            item {
                Text(
                    text = "Hiệu suất 7 ngày gần nhất",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
                )
                LearningPerformanceChart(weeklyStats)
            }
        }
    }
}

// 🔥 COMPOSABLE BỘ THẺ "BẢO TÀNG KÝ ỨC" ĐƯỢC THIẾT KẾ ĐẸP MẮT
@Composable
fun MemoryMuseumCard(onClick: () -> Unit) {
    CalmCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = Calm.NeutralMist
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = "Bảo tàng ký ức",
                tint = Calm.Primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Bảo tàng nụ cười",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Xem lại những khoảnh khắc thức dậy của bạn 📸",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

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
        border = androidx.compose.foundation.BorderStroke(
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
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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

@Composable
fun StatCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    color: Color
) {
    CalmCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp), // Tăng nội padding cho thoáng
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (subValue.isNotEmpty()) {
                    Text(text = subValue, style = MaterialTheme.typography.bodySmall, color = color)
                }
            }
        }
    }
}

@Composable
fun SmallStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    CalmCard(
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}