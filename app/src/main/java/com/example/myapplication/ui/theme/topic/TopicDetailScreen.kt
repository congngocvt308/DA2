package com.example.myapplication.ui.theme.topic

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TopicDetailScreen(
    topicId: Int,
    viewModel: TopicDetailViewModel = viewModel(),
    onBackClick: () -> Unit,
    //onQuestionClick: (Int) -> Unit // Nếu muốn xem chi tiết câu hỏi
) {
    LaunchedEffect(topicId) {
        viewModel.loadQuestions(topicId)
    }

    val allQuestions by viewModel.questions.collectAsState()
    val filteredQuestionsList by viewModel.filteredQuestions.collectAsStateWithLifecycle()
    val currentSearchQuery = viewModel.searchQuery.value
    val onQueryChange: (String) -> Unit = { viewModel.searchQuery.value = it }
    var showAddDialog by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearching) {
        if (isSearching) {
            // Yêu cầu focus khi isSearching chuyển thành true
            focusRequester.requestFocus()
        }
    }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Content chính ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 106.dp) // Đặt padding top bằng chiều cao của TopBar
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
                modifier = Modifier.weight(1f), // Để cột cuộn được và đặt button ở dưới
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
                        onDelete = { viewModel.deleteQuestion(question.id) }
                    )
                }
            }
        }

        // --- Top Bar tùy chỉnh ---
        TopicDetailTopBar(
            isSearching = isSearching,
            searchQuery = currentSearchQuery, // Truyền giá trị hiện tại
            onSearchToggle = {
                isSearching = !isSearching
                if (!isSearching) {
                    onQueryChange("") // Gọi hàm cập nhật
                    focusManager.clearFocus()
                }
            },
            onQueryChange = onQueryChange, // Truyền hàm cập nhật
            onBackClick = onBackClick,
            showMenu = showMenu,
            onMenuToggle = { showMenu = !showMenu },
            focusManager = focusManager,
            focusRequester = focusRequester
        )

        // --- Bottom Button ---
        Button(
            onClick = {
                viewModel.resetDraft()
                showAddDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .align(Alignment.BottomCenter) // Đặt ở dưới cùng
                .fillMaxWidth()
                .padding(16.dp, 16.dp, 16.dp, 30.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Thêm câu hỏi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }

        // --- Dialog và Composable Phụ ---
        if (showAddDialog) {
            //
            AddQuestionDialog(
                viewModel = viewModel,
                onDismiss = { showAddDialog = false },
                onSaveSuccess = { showAddDialog = false }
            )
        }
    }
}

// Composable cho Top Bar tùy chỉnh
@Composable
fun TopicDetailTopBar(
    isSearching: Boolean,
    searchQuery: String,
    onSearchToggle: () -> Unit,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    showMenu: Boolean,
    onMenuToggle: () -> Unit,
    focusManager: FocusManager,
    focusRequester: FocusRequester
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(106.dp)
            .padding(top = 50.dp) // Đặt padding top
    ) {
        Crossfade(targetState = isSearching, modifier = Modifier.fillMaxWidth()) { searching ->
            if (searching) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Đóng tìm kiếm", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    OutlinedTextField(
                        shape = CircleShape,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                            }
                        ),
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                            .height(50.dp)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Tìm câu hỏi...", color = MaterialTheme.colorScheme.surfaceVariant) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon Quay lại
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    // Tiêu đề (có thể thay bằng tên chủ đề nếu có)
                    Text(
                        text = "Chi tiết Chủ đề", // Có thể hiển thị tên chủ đề thực tế
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Icons
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm", tint = MaterialTheme.colorScheme.onSurface)
                    }

                    Box {
                        IconButton(onClick = onMenuToggle) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = onMenuToggle
                        ) {
                            DropdownMenuItem(
                                text = { Text("Chỉnh sửa") },
                                onClick = {
                                    onMenuToggle()
                                    // Logic xử lý đổi tên
                                },
                                trailingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                            )

                            DropdownMenuItem(
                                text = { Text("Chia sẻ") },
                                onClick = {
                                    onMenuToggle()
                                    // Logic xử lý đổi tên
                                },
                                trailingIcon = { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                            )

                            DropdownMenuItem(
                                text = { Text("Xóa", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onMenuToggle()
                                    // Logic xử lý xóa
                                },
                                trailingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // Padding cuối
                }
            }
        }
    }
}