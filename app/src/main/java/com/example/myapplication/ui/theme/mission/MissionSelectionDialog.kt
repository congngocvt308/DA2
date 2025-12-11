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
import com.example.myapplication.ui.theme.mission.MissionViewModel

@Composable
fun MissionSelectionDialog(
    initialCount: Int,
    initialSelection: List<MissionQuestion>,
    onDismiss: () -> Unit,
    onConfirm: (Int, List<MissionQuestion>) -> Unit,
    viewModel: MissionViewModel = viewModel()
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
            ) {
                MissionSelectionContent(
                    initialCount = initialCount,
                    initialSelection = initialSelection,
                    viewModel = viewModel,
                    onDismiss = onDismiss,
                    onConfirm = onConfirm
                )
            }
        }
    }
}

@Composable
fun MissionSelectionContent(
    initialCount: Int,
    initialSelection: List<MissionQuestion>,
    viewModel: MissionViewModel,
    onDismiss: () -> Unit,
    onConfirm: (Int, List<MissionQuestion>) -> Unit
) {
    var questionCount by remember { mutableFloatStateOf(if (initialCount > 0) initialCount.toFloat() else 0f) }
    val dbTopics by viewModel.missionTopics.collectAsState()
    var localTopics by remember { mutableStateOf<List<MissionTopic>>(emptyList()) }

    LaunchedEffect(dbTopics) {
        if (dbTopics.isNotEmpty()) {
            val selectedIds = initialSelection.map { it.id }.toSet()

            localTopics = dbTopics.map { topic ->
                val updatedQuestions = topic.questions.map { question ->
                    val isSelected = selectedIds.contains(question.id)
                    question.copy(isSelected = isSelected)
                }
                val isTopicSelected = updatedQuestions.isNotEmpty() && updatedQuestions.all { it.isSelected }
                val shouldExpand = updatedQuestions.any { it.isSelected }

                topic.copy(
                    questions = updatedQuestions,
                    isSelected = isTopicSelected,
                    isExpanded = shouldExpand
                )
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 700.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${questionCount.toInt()}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("Số câu hỏi", fontSize = 16.sp, color = MaterialTheme.colorScheme.tertiary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = questionCount,
                        onValueChange = { questionCount = it },
                        valueRange = 0f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.onSurface,
                            activeTrackColor = MaterialTheme.colorScheme.onSurface,
                            inactiveTrackColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                LazyColumn(modifier = Modifier.padding(vertical = 8.dp)) {
                    item {
                        Text(
                            text = "Chọn câu hỏi",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }

                    items(localTopics) { topic ->
                        TopicItemRow(
                            topic = topic,
                            onExpandClick = {
                                localTopics = localTopics.map {
                                    if (it.id == topic.id) it.copy(isExpanded = !it.isExpanded) else it
                                }
                            },
                            onSelectTopic = { isSelected ->
                                localTopics = localTopics.map { t ->
                                    if (t.id == topic.id) {
                                        val updatedQuestions = t.questions.map { q -> q.copy(isSelected = isSelected) }
                                        t.copy(isSelected = isSelected, questions = updatedQuestions)
                                    } else t
                                }
                            }
                        )

                        if (topic.isExpanded) {
                            topic.questions.forEach { question ->
                                QuestionItemRow(
                                    question = question,
                                    onQuestionClick = {
                                        localTopics = localTopics.map { t ->
                                            if (t.id == topic.id) {
                                                val updatedQuestions = t.questions.map { q ->
                                                    if (q.id == question.id) q.copy(isSelected = !q.isSelected) else q
                                                }
                                                val allSelected = updatedQuestions.all { it.isSelected }
                                                t.copy(questions = updatedQuestions, isSelected = allSelected)
                                            } else t
                                        }
                                    }
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                thickness = 0.5.dp
                            )
                        }
                    }

                    if (localTopics.isEmpty()) {
                        item {
                            Text(
                                "Chưa có dữ liệu. Hãy thêm Chủ đề và Câu hỏi trước.",
                                modifier = Modifier.padding(20.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val allSelectedQuestions = localTopics.flatMap { it.questions }.filter { it.isSelected }
                    onConfirm(questionCount.toInt(), allSelectedQuestions)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
            .clickable { onExpandClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (topic.isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onSelectTopic(!topic.isSelected) }
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}