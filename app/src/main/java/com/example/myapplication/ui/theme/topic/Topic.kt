package com.example.myapplication.ui.theme.topic

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Surface

val DarkBackground = Color(0xFF1C1C1E)
@Composable
fun Topic(
    viewModel: TopicViewModel = viewModel(),
    onTopicClicked: (TopicData) -> Unit = {},
    onMenuClicked: () -> Unit = {},
    onAddNewTopic: () -> Unit = {}
) {
    var isSearching by remember { mutableStateOf(false) }
    val topics by viewModel.filteredTopics.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
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
                onMenuClicked = onMenuClicked
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewTopic,
                containerColor = Color(0xFFE50043),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Thêm Chủ đề",
                    tint = Color.White)
            }
        },
        containerColor = Color.Black
    ) { padding ->
        TopicList(
            topics = topics,
            onTopicClick = onTopicClicked,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun TopicAppBar(
    isSearching: Boolean,
    onSearchToggle: () -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onMenuClicked: () -> Unit
) {
    Surface(
        color = DarkBackground,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Crossfade(targetState = isSearching, modifier = Modifier.fillMaxWidth()) { searching ->
            if (searching) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, end = 8.dp),
                        placeholder = { Text("Tìm chủ đề...", color = Color.Gray) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.White,
                            unfocusedIndicatorColor = Color.Gray,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Xóa", tint = Color.Gray)
                                }
                            }
                        }
                    )
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng", tint = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thư viện Chủ đề",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 16.dp).weight(1f)
                    )
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm", tint = Color.White)
                    }
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Tùy chọn", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TopicList(
    topics: List<TopicData>,
    onTopicClick: (TopicData) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (topics.isEmpty()) {
            item {
                Text(
                    text = "Không tìm thấy chủ đề nào phù hợp.",
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        items(topics, key = { it.id }) { topic ->
            TopicCard(topic = topic, onClick = { onTopicClick(topic) })
        }
    }
}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun TopicTabScreenPreview() {
    Topic(
        onTopicClicked = {},
        onMenuClicked = {},
        onAddNewTopic = {}
    )
}