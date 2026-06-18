package com.example.myapplication.ui.theme.topic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.AiMatrixConfig
import com.example.myapplication.ui.theme.components.SearchableTopBar
import kotlinx.coroutines.launch

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun TopicScreen(
    viewModel: TopicViewModel = viewModel(),
    onNavigateToDetail: (Int) -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }
    var isFabMenuOpen by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showAiDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val matrixConfig by viewModel.matrixConfig.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val topicList by viewModel.filteredTopics.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDocuments by viewModel.selectedDocuments.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = isSearching || isFabMenuOpen) {
        if (isFabMenuOpen) isFabMenuOpen = false
        else if (isSearching) {
            isSearching = false
            viewModel.searchQuery.value = ""
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchableTopBar(
                title = "Thư viện Chủ đề",
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchToggle = {
                    isSearching = !isSearching
                    if (!isSearching) {
                        viewModel.onSearchQueryChange("")
                        focusManager.clearFocus()
                    }
                },
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onBackClick = null,
                actions = {}
            )

            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (topicList.isEmpty()) {
                    item {
                        Text(
                            text = "Không tìm thấy chủ đề nào phù hợp.",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                itemsIndexed(items = topicList, key = { _, topic -> topic.id }) { _, topic ->
                    TopicCard(topic = topic, onClick = { onNavigateToDetail(topic.id) })
                }
            }
        }

        if (isFabMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabMenuOpen = false }
            )
        }

        AnimatedVisibility(
            visible = !isSearching, // Chỉ hiện khi KHÔNG tìm kiếm
            enter = fadeIn() + scaleIn(), // Hiệu ứng hiện ra (phóng to)
            exit = fadeOut() + scaleOut(), // Hiệu ứng biến mất (thu nhỏ)
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
        ) {
            FabSpeedDial(
                isMenuOpen = isFabMenuOpen,
                onToggleMenu = { isFabMenuOpen = !isFabMenuOpen },
                onAddNewTopic = {
                    isFabMenuOpen = false
                    showAddTopicDialog = true
                },
                onQuickTopicClick = {
                    isFabMenuOpen = false
                },
                onAiClick = {
                    isFabMenuOpen = false
                    showAiDialog = true
                }
                // Bỏ modifier cũ ở đây vì đã đưa lên AnimatedVisibility
            )
        }

        if (showAddTopicDialog) {
            QuickTopicDialog(
                initialName = "",
                onDismissRequest = { showAddTopicDialog = false },
                onSave = { topicName ->
                    viewModel.addNewTopic(topicName)
                    showAddTopicDialog = false
                }
            )
        }

        if (showAiDialog) {
            AiCreateQuestionBottomSheet(
                isLoading = isLoading,
                matrixConfig = matrixConfig,
                selectedDocuments = selectedDocuments, // Đẩy danh sách hàng chờ xuống Sheet
                onDismiss = {
                    showAiDialog = false
                    viewModel.clearState()
                },
                onAddDocument = { uri, name, isPdf ->
                    viewModel.addDocumentToQueue(uri, name, isPdf)
                },
                onRemoveDocument = { id ->
                    viewModel.removeDocumentFromQueue(id)
                },
                onUpdatePageConfig = { id, config ->
                    viewModel.updateDocumentPageConfig(id, config)
                },
                onProcessAndCompress = {
                    viewModel.processAndCompressQueue() // Gọi chạy tiến trình nén hàng loạt kiểm thử
                },
                onTopicNameChange = { newName -> viewModel.updateTopicName(newName) },
                onPresetSelect = { isTryHard -> viewModel.applyPreset(isTryHard) },
                onMatrixSliderChange = { ez, mid, hard -> viewModel.updateMatrixSliders(ez, mid, hard) },
                onStartGenerationSuccess = {
                    viewModel.finalizeAndGenerateQuestions(
                        onSuccess = { actualTopicId, extractedText, easy, mid, hard ->
                            showAiDialog = false
                            TopicStreamBuffer.pendingRequest = TopicStreamBuffer.StreamRequest(
                                actualTopicId, extractedText, easy, mid, hard
                            )
                            onNavigateToDetail(actualTopicId)
                            // topicDetailViewModel.startQuestionsStreaming(extractedText, easy, mid, hard)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun FabSpeedDial(
    isMenuOpen: Boolean,
    onToggleMenu: () -> Unit,
    onAddNewTopic: () -> Unit,
    onQuickTopicClick: () -> Unit,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 45f else 0f,
        label = "FabRotation"
    )

    Box(modifier = modifier.padding(16.dp)) {
        AnimatedVisibility(
            visible = isMenuOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 80.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    FabMenuItem(
                        icon = Icons.Default.FlashOn,
                        text = "Chủ đề có sẵn",
                        onClick = onQuickTopicClick
                    )
                    FabMenuItem(
                        icon = Icons.Filled.Style,
                        text = "Thêm chủ đề mới",
                        onClick = onAddNewTopic
                    )
                    FabMenuItem(
                        icon = Icons.Filled.AutoAwesome,
                        text = "Tạo bằng AI",
                        onClick = onAiClick
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleMenu,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.size(56.dp).align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Tạo mới",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp).rotate(rotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}