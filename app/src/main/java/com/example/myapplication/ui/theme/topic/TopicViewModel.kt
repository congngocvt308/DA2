package com.example.myapplication.ui.theme.topic

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.TopicData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TopicViewModel(): ViewModel() {
    private val _topics = MutableStateFlow(
        listOf(
            TopicData(1, "Tích phân từng phần", 15),
            TopicData(2, "Từ vựng IELTS - Môi trường", 48),
            TopicData(3, "Công thức Lượng giác", 22),
            TopicData(4, "Khởi tạo Project Flutter", 5)
        )
    )

    val searchQuery = mutableStateOf("")
    val filteredTopics: StateFlow<List<TopicData>> = combine(
        _topics,
        snapshotFlow{ searchQuery.value }
    ){ topics, query ->
        if (query.isBlank()) topics
        else {
            topics.filter { topic ->
                topic.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
    )
    fun addNewTopic(nameAdd: String) {
        val newTopic = TopicData(
            id = (System.currentTimeMillis() % 10000).toInt(),
            name = nameAdd,
            questionCount = 0
        )
        val currentList = _topics.value.toMutableList()
        currentList.add(newTopic)
        _topics.value = currentList
    }
}