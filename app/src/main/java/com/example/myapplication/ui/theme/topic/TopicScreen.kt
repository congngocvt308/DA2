package com.example.myapplication.ui.theme.topic

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.rotate
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.theme.alarm.FabMenuItem
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import com.example.myapplication.ui.theme.alarm.QuickAlarmDialog

@Composable
fun TopicScreen(
    viewModel: TopicViewModel = viewModel(),
    onNavigateToSettings: (Int) -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }
    var isFabMenuOpen by remember { mutableStateOf(false) }
    var showQuickDialog by remember { mutableStateOf(false) }

    val topicList by viewModel.filteredTopics.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    BackHandler(enabled = isSearching || isFabMenuOpen) {
        if (isFabMenuOpen) isFabMenuOpen = false
        if (isSearching) {
            isSearching = false
            viewModel.searchQuery.value = ""
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        Column(modifier = Modifier.fillMaxSize()) {
            TopicAppBar(
                isSearching = isSearching,
                onSearchToggle = {
                    isSearching = !isSearching
                    if (!isSearching) {
                        viewModel.searchQuery.value = ""
                        focusManager.clearFocus()
                    }
                },
                searchQuery = viewModel.searchQuery.value,
                onQueryChange = { viewModel.searchQuery.value = it },
                focusRequester = focusRequester
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (topicList.isEmpty()) {
                    item {
                        Text(
                            text = "Không tìm thấy chủ đề nào phù hợp.",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                itemsIndexed(items = topicList, key = { index, topic -> topic.id }
                ) { index, topic ->
                    TopicCard(topic = topic, onClick = { onNavigateToSettings(topic.id) })
                }
            }
        }

        if (isFabMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabMenuOpen = false }
            )
        }

        FabSpeedDial(
            isMenuOpen = isFabMenuOpen,
            onToggleMenu = { isFabMenuOpen = !isFabMenuOpen },
            onAddNewTopic = {
                isFabMenuOpen = false
                showQuickDialog = true
            },
            onQuickTopicClick = {
                isFabMenuOpen = false
                onNavigateToSettings(-1)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
        )

        if (showQuickDialog) {
            QuickTopicDialog(
                onDismissRequest = { showQuickDialog = false },
                onSave = { topicId ->
                    showQuickDialog = false
                }
            )
        }
    }
}

@Composable
fun TopicAppBar(
    isSearching: Boolean,
    onSearchToggle: () -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    val focusManager = LocalFocusManager.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(106.dp)
            .padding(top = 50.dp)
    ) {
        Crossfade(targetState = isSearching, modifier = Modifier.fillMaxWidth()) { searching ->
            if (searching) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Đóng", tint = MaterialTheme.colorScheme.onSurface)
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
                        placeholder = { Text("Tìm chủ đề...", color = MaterialTheme.colorScheme.surfaceVariant) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                    Text(
                        text = "Thư viện Chủ đề",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 16.dp).weight(1f)
                    )

                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun FabSpeedDial(
    isMenuOpen: Boolean,
    onToggleMenu: () -> Unit,
    onAddNewTopic: () -> Unit,
    onQuickTopicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 45f else 0f,
        label = "FabRotation"
    )

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = isMenuOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.width(250.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    FabMenuItem(
                        Icons.Default.FlashOn,
                        "Chủ đề có sẵn",
                        onClick = onQuickTopicClick
                    )

                    FabMenuItem(
                        Icons.Filled.Style,
                        "Thêm chủ đề",
                        onClick = onAddNewTopic
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleMenu,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Tạo mới",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(rotation)
            )
        }
    }
}