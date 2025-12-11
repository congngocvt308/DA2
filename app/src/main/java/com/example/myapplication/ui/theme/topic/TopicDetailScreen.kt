package com.example.myapplication.ui.theme.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.myapplication.ui.theme.components.SearchableTopBar

@Composable
fun TopicDetailScreen(
    viewModel: TopicDetailViewModel = viewModel(),
    onBackClick: () -> Unit,
) {
    val allQuestions by viewModel.questions.collectAsState()
    val currentTopicName by viewModel.topicName.collectAsState()
    val filteredQuestionsList by viewModel.filteredQuestions.collectAsStateWithLifecycle()
    val currentSearchQuery = viewModel.searchQuery.collectAsState().value
    var showAddDialog by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var showEditTopicDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 106.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Tất cả: ${allQuestions.size}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.tertiary, thickness = 1.dp, modifier = Modifier.width(100.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredQuestionsList.isEmpty() && currentSearchQuery.isNotBlank()) {
                    item {
                        Text(
                            text = "Không tìm thấy câu hỏi nào phù hợp với '$currentSearchQuery'.",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }   else {
                    if (filteredQuestionsList.isEmpty() && currentSearchQuery.isBlank()) {
                        item {
                            Text(
                                text = "Chủ đề này chưa có câu hỏi nào.",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                items(filteredQuestionsList, key = { it.id }) { question ->
                    QuestionCard(
                        question = question,
                        onDelete = { viewModel.deleteQuestion(question.id) },
                        onClick = {
                            viewModel.startEditing(question)
                            showAddDialog = true
                        }
                    )
                }
            }
        }

        SearchableTopBar(
            title = "Chi tiết Chủ đề",
            isSearching = isSearching,
            searchQuery = currentSearchQuery,
            onSearchToggle = {
                isSearching = !isSearching
                if (!isSearching) viewModel.searchQuery.value = ""
            },
            onQueryChange = { viewModel.searchQuery.value = it },
            onBackClick = onBackClick,
            actions = {
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            Icons.Default.MoreVert,
                            "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Chỉnh sửa") },
                            onClick = {
                                showMenu = false
                                showEditTopicDialog = true
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Chia sẻ") },
                            onClick = {
                                { showMenu = false }
                                // Logic xử lý đổi tên
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Share,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Xóa", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        )

        if (showEditTopicDialog) {
            QuickTopicDialog(
                initialName = currentTopicName,
                onDismissRequest = { showEditTopicDialog = false },
                onSave = { newName ->
                    viewModel.renameTopic(newName)
                    showEditTopicDialog = false
                }
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Xóa chủ đề?") },
                text = { Text("Bạn có chắc muốn xóa chủ đề '$currentTopicName' và tất cả câu hỏi bên trong không?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCurrentTopic(onDeleted = onBackClick)
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Xóa", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Hủy", color = MaterialTheme.colorScheme.tertiary)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.tertiary
            )
        }

        Button(
            onClick = {
                viewModel.resetDraft()
                showAddDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 30.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Thêm câu hỏi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }

        if (showAddDialog) {
            AddQuestionDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false },
                onSaveSuccess = { showAddDialog = false }
            )
        }
    }
}