package com.example.myapplication.ui.theme.mission

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
import com.example.myapplication.data.MissionQuestion
import com.example.myapplication.data.MissionTopic
import com.example.myapplication.data.MissionUiState
import com.example.myapplication.ui.theme.mission.MissionViewModel

@Composable
fun MissionSelectionDialog(
    initialCount: Int,
    initialSelectionIds: Set<Int>, // Truyền Set ID Int vào đây
    initialTopicIds: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Int, List<MissionQuestion>, List<MissionTopic>) -> Unit,
    viewModel: MissionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = 700.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Nhiệm vụ",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Slider chọn số lượng
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ){
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${uiState.questionCount}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Số câu hỏi (tối đa: $totalSelected)", color = MaterialTheme.colorScheme.tertiary)

                    if (totalSelected > 0) {
                        Slider(
                            value = uiState.questionCount.toFloat(),
                            onValueChange = { onCountChange(it.toInt()) },
                            valueRange = 0f..totalSelected.toFloat(),
                            steps = if (totalSelected > 1) totalSelected - 1 else 0,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.onSurface,
                                activeTrackColor = MaterialTheme.colorScheme.onSurface,
                                inactiveTrackColor = MaterialTheme.colorScheme.tertiary
                            )
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

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ){
                LazyColumn(modifier = Modifier.padding(vertical = 8.dp)){
                    items(uiState.topics) { topic ->
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

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Hoàn tất", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Nút Checkbox (đã chuyển sang dùng val từ ViewModel)
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (topic.isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onSelectTopic(!topic.isSelected) } // Gọi sự kiện chọn tất cả
                    .background(
                        color = if (topic.isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (topic.isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

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
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
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
            .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Check nhỏ cho từng câu hỏi
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = if (question.isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(20.dp)
                .border(
                    1.dp,
                    if (question.isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                    RoundedCornerShape(10.dp)
                )
                .padding(3.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = question.text,
            color = if (question.isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiary,
            fontSize = 14.sp,
            maxLines = 1, // Tăng lên 2 dòng để hiển thị câu hỏi dài tốt hơn
            overflow = TextOverflow.Ellipsis
        )
    }
}