package com.example.myapplication.ui.theme.stats

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
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
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.SrsStat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val context = LocalContext.current
    val statsDao = remember(context) {
        AppDatabase.getDatabase(context).statisticsDao()
    }

    val viewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(statsDao)
    )

    val score by viewModel.wakeUpScore.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyAccuracy.collectAsStateWithLifecycle(initialValue = emptyList())
    val srsStats by viewModel.srsDistribution.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val questions by viewModel.filteredQuestions.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()

    if (selectedStatus != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.selectSrsStatus(null) }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
                Text(
                    text = "Danh sách câu hỏi: $selectedStatus",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    items(questions) { question ->
                        ListItem(
                            // 🚨 KIỂM TRA: Nếu 'questionText' báo đỏ, hãy đổi thành tên cột
                            // trong QuestionEntity của bạn (ví dụ: question.text hoặc question.content)
                            headlineContent = { Text(question.prompt) },
                            supportingContent = { Text("ID: ${question.questionId}") }
                        )
                        HorizontalDivider()
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
            .padding(top = 50.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp) // Tăng vertical lên chút cho đẹp
                .height(35.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Phân tích hệ thống",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                userStats?.let { stats ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp, start = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Thẻ hiện Streak (Chuỗi ngày)
                        SmallStatCard(
                            label = "Chuỗi ngày",
                            value = "${stats.currentStreak} 🔥",
                            modifier = Modifier.weight(1f)
                        )
                        // Thẻ hiện Tổng điểm
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
                    color = Color(0xFF4CAF50)
                )
            }

            // 2. Hiển thị Trạng thái trí nhớ (SRS)
            item {
                Text(
                    text = "Trạng thái trí nhớ (SRS)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
                )
                SrsDistributionSection(srsStats) { status ->
                    viewModel.selectSrsStatus(status)
                }
            }

            // 3. Hiển thị Hiệu suất học tập 7 ngày qua
            item {
                Text(
                    text = "Hiệu suất 7 ngày gần nhất",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
                )
                LearningPerformanceChart(weeklyStats)
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

    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        // Sử dụng màu nền tối hơn một chút để làm nổi bật thanh tiến trình
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tiêu đề tổng quan
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Phân phối kiến thức", style = MaterialTheme.typography.titleMedium)
                Text("$totalQuestions câu hỏi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // 2. Thanh Stacked Progress Bar (Phần lõi)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp)) // Bo tròn toàn bộ thanh
                    .background(Color.Gray.copy(alpha = 0.2f)) // Màu nền khi chưa có dữ liệu
            ) {
                if (totalQuestions > 0) {
                    // Phần Mastered (Xanh lá)
                    Box(
                        modifier = Modifier
                            .weight((mastered.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFF4CAF50))
                            .clickable { onStatusClick("Mastered") }
                    )
                    // Phần Learning (Vàng cam)
                    Box(
                        modifier = Modifier
                            .weight((learning.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFFFFC107))
                            .clickable { onStatusClick("Learning") }
                    )
                    // Phần New (Xanh dương nhạt/Xám)
                    Box(
                        modifier = Modifier
                            .weight((new.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFF90A4AE))
                            .clickable { onStatusClick("New") }
                    )
                }
            }

            // 3. Chú thích (Legend) bên dưới
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendItem(color = Color(0xFF4CAF50), label = "Đã thuộc ($mastered)")
                LegendItem(color = Color(0xFFFFC107), label = "Đang học ($learning)")
                LegendItem(color = Color(0xFF90A4AE), label = "Mới ($new)")
            }
        }
    }
}

// Hàm vẽ chú thích nhỏ
@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun LearningPerformanceChart(stats: List<Pair<String, Float>>) {
    val goalPercentage = 0.8f
    val labelAreaHeight = 32.dp // 🚨 Cố định chiều cao vùng nhãn X để tính toán

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // 1. LỚP NỀN: Đường lưới (Grid Lines)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = labelAreaHeight), // 🚨 Chừa đúng khoảng trống cho nhãn X
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                GridLine("100%")
                GridLine("50%")
                // Vạch 0% giờ đây sẽ nằm chính xác ở đáy của vùng Graph
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "0%", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(35.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                }
            }
            // 3. LỚP CỘT VÀ NHÃN (Foreground)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 35.dp), // Thụt vào để tránh nhãn trục Y
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.forEach { (label, accuracy) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        // Vùng hiển thị cột (Graph Area)
                        Box(
                            modifier = Modifier
                                .weight(1f) // Chiếm toàn bộ không gian phía trên
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter // 🚨 Ép cột mọc từ đáy
                        ) {
                            if (accuracy > 0f) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(accuracy * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (accuracy >= goalPercentage) Color(0xFF4CAF50) else Color.Gray
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .fillMaxHeight(accuracy) // Cao theo tỷ lệ chuẩn
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        if (accuracy >= goalPercentage) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                                        if (accuracy >= goalPercentage) Color(0xFF81C784) else Color(0xFFFFB74D)
                                                    )
                                                ),
                                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                }
                            } else {
                                // Nếu là 0%, vẽ một vạch mờ chính xác tại vạch 0%
                                Box(modifier = Modifier.width(16.dp).height(1.dp).background(Color.Gray.copy(0.3f)))
                            }
                        }

                        // Vùng nhãn trục X (Label Area)
                        Box(
                            modifier = Modifier.height(labelAreaHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (label == "Nay") MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = if (label == "Nay") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// Hàm vẽ đường lưới ngang
@Composable
fun GridLine(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.width(30.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    color: Color // Bây giờ nó là Compose Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        // .copy() bây giờ sẽ hoạt động
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                // Color.Gray (viết hoa chữ G) là của Compose
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
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