package com.cong.smartalarm.ui.theme.stats

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
import com.cong.smartalarm.data.AppDatabase
import com.cong.smartalarm.data.SrsStat
import com.cong.smartalarm.ui.theme.facedetection.MemoryMuseumScreen // 🔥 Import màn hình Bảo tàng ký ức

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onNavigateToMuseum: () -> Unit) {
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
                    color = Color(0xFF4CAF50)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.1f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF4081).copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Collections,
                contentDescription = "Bảo tàng ký ức",
                tint = Color(0xFFFF4081),
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
                    color = Color(0xFFFF4081)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.05f)
        ),
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

            // 2. Thanh Stacked Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                if (totalQuestions > 0) {
                    Box(
                        modifier = Modifier
                            .weight((mastered.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFF4CAF50))
                            .clickable { onStatusClick("Mastered") }
                    )
                    Box(
                        modifier = Modifier
                            .weight((learning.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFFFFC107))
                            .clickable { onStatusClick("Learning") }
                    )
                    Box(
                        modifier = Modifier
                            .weight((new.toFloat() / totalQuestions).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFF90A4AE))
                            .clickable { onStatusClick("New") }
                    )
                }
            }

            // 3. Chú thích
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
    val labelAreaHeight = 32.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.05f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = labelAreaHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                GridLine("100%")
                GridLine("50%")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "0%", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.width(35.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 35.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.forEach { (label, accuracy) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
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
                                            .fillMaxHeight(accuracy)
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
                                Box(modifier = Modifier.width(16.dp).height(1.dp).background(Color.Gray.copy(0.3f)))
                            }
                        }

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
    color: Color
) {
    Card(
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
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.05f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
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