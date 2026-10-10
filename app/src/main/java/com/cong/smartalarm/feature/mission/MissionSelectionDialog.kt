package com.cong.smartalarm.feature.mission

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.cong.smartalarm.core.designsystem.component.CalmSlider
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.core.designsystem.theme.Calm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.feature.mission.MissionQuestion
import com.cong.smartalarm.feature.mission.MissionTopic
import com.cong.smartalarm.feature.mission.MissionUiState
import com.cong.smartalarm.feature.mission.MissionViewModel

@Composable
fun MissionSelectionDialog(
    initialCount: Int,
    initialSelectionIds: Set<Int>, // Truyền Set ID Int vào đây
    initialTopicIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Int, List<MissionQuestion>, List<MissionTopic>) -> Unit,
    viewModel: MissionViewModel = viewModel(factory = MissionViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Khởi tạo data đúng 1 lần khi mở Dialog
    LaunchedEffect(Unit) {
        viewModel.initData(initialCount, initialSelectionIds, initialTopicIds)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().clickable { onDismiss() }, contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
            ) {
                MissionSelectionContent(
                    uiState = uiState,
                    onTopicClick = { id, selectAll -> viewModel.toggleTopic(id, selectAll) },
                    onQuestionClick = { tId, qId -> viewModel.toggleQuestion(tId, qId) },
                    onExpandClick = { viewModel.toggleExpansion(it) },
                    onCountChange = { viewModel.updateCount(it) },
                    onDismiss = onDismiss,
                    onConfirm = {
                        val selected =
                            uiState.topics.flatMap { it.questions }.filter { it.isSelected }
                        onConfirm(uiState.questionCount, selected, uiState.topics)
                    }
                )
            }
        }
    }
}

@Composable
fun MissionSelectionContent(
    uiState: MissionUiState,
    onTopicClick: (Int, Boolean) -> Unit,
    onQuestionClick: (Int, Int) -> Unit,
    onExpandClick: (Int) -> Unit,
    onCountChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalSelected = remember(uiState.topics) {
        uiState.topics.flatMap { it.questions }.count { it.isSelected }
    }

    CalmCard(
        modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nhiệm vụ",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Slider chọn số lượng
            CalmCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ){
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${uiState.questionCount}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Số câu hỏi (tối đa: $totalSelected)", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (totalSelected > 0) {
                        CalmSlider(
                            value = uiState.questionCount.toFloat(),
                            onValueChange = { onCountChange(it.toInt()) },
                            valueRange = 0f..totalSelected.toFloat(),
                            steps = if (totalSelected > 1) totalSelected - 1 else 0
                        )
                    } else {
                        Text(
                            text = "Vui lòng chọn ít nhất 1 câu hỏi bên dưới",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CalmCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ){
                LazyColumn(modifier = Modifier.padding(vertical = 8.dp)){
                    items(uiState.topics, key = { it.id }) { topic ->
                        TopicItemRow(
                            topic = topic,
                            onExpandClick = { onExpandClick(topic.id) },
                            onSelectTopic = { onTopicClick(topic.id, it) }
                        )
                        if (topic.isExpanded) {
                            topic.questions.forEach { q ->
                                QuestionItemRow(q, onQuestionClick = { onQuestionClick(topic.id, q.id) })
                            }
                        }
                    }
                }
            }
            // Section: Danh sách Topic & Question

            Spacer(modifier = Modifier.height(24.dp))

            CalmButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Hoàn tất", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun TopicItemRow(
    topic: MissionTopic,
    onExpandClick: () -> Unit,
    onSelectTopic: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick() } // Click vào hàng để đóng/mở
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            // Nút Checkbox (đã chuyển sang dùng val từ ViewModel)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onSelectTopic(!topic.isSelected) }, // Gọi sự kiện chọn tất cả
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (topic.isSelected) "Bỏ chọn cả chủ đề" else "Chọn cả chủ đề",
                    tint = if (topic.isSelected) Color.White else Color.Transparent,
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (topic.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.5.dp,
                            if (topic.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = topic.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Icon mũi tên lên/xuống
        Icon(
            imageVector = if (topic.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (topic.isExpanded) "Thu gọn" else "Mở rộng",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuestionItemRow(
    question: MissionQuestion,
    onQuestionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onQuestionClick() }
            .heightIn(min = 48.dp)
            .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Check nhỏ cho từng câu hỏi
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = if (question.isSelected) "Đã chọn" else "Chưa chọn",
            tint = if (question.isSelected) Color.White else Color.Transparent,
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (question.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    RoundedCornerShape(6.dp)
                )
                .border(
                    1.5.dp,
                    if (question.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(6.dp)
                )
                .padding(3.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = question.text,
            color = if (question.isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            maxLines = 1, // Tăng lên 2 dòng để hiển thị câu hỏi dài tốt hơn
            overflow = TextOverflow.Ellipsis
        )
    }
}