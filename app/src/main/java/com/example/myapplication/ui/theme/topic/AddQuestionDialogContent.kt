package com.example.myapplication.ui.theme.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AddQuestionDialog(
    viewModel: TopicDetailViewModel,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val questionText by viewModel.draftQuestionText.collectAsState()
    val answers by viewModel.draftAnswers.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 700.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { onDismiss() }
                        )

                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Lưu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable {
                                viewModel.saveQuestion()
                                onSaveSuccess()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { viewModel.onQuestionTextChange(it) },
                        label = { Text("Câu hỏi", color = MaterialTheme.colorScheme.tertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(answers) { index, answer ->
                            AnswerInputRow(
                                text = answer.text,
                                isCorrect = answer.isCorrect,
                                onTextChange = { viewModel.onAnswerTextChange(index, it) },
                                onSelectCorrect = { viewModel.onSelectCorrectAnswer(index) },
                                onDelete = { viewModel.onRemoveAnswerLine(index) }
                            )
                        }
                        if (answers.size < 5) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .clickable { viewModel.onAddAnswerLine() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.tertiary)

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text("Thêm câu trả lời", color = MaterialTheme.colorScheme.tertiary, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.saveQuestion()
                            onSaveSuccess()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Thêm câu hỏi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerInputRow(
    text: String,
    isCorrect: Boolean,
    onTextChange: (String) -> Unit,
    onSelectCorrect: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = isCorrect,
            onClick = onSelectCorrect,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.secondary,
                unselectedColor = MaterialTheme.colorScheme.tertiary
            )
        )

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    if (isCorrect) "Câu trả lời đúng" else "Điền câu trả lời",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .weight(1f)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .background(Color.Transparent),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(8.dp)
        )

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Xóa",
                tint = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        }
    }
}