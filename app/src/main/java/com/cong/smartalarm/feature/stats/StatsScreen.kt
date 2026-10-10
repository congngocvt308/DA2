package com.cong.smartalarm.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.feature.museum.MemoryMuseumScreen // 🔥 Import màn hình Bảo tàng ký ức

/** Màn hình Thống kê: chỉ số tỉnh táo, bảo tàng nụ cười, SRS và hiệu suất 7 ngày. */
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
