package com.example.myapplication.Data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TopicData(
    val id: Long,
    val name: String,
    val questionCount: Int
)

class TopicViewModel : ViewModel() {
    private val allTopics = getSampleTopics()
    val searchQuery = mutableStateOf("")
    val filteredTopics: StateFlow<List<TopicData>> = snapshotFlow { searchQuery.value }
        .map { query ->
            if (query.isBlank()) {
                allTopics
            } else {
                allTopics.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = allTopics
        )
    private fun getSampleTopics() = listOf(
        TopicData(1, "Tích phân từng phần", 15),
        TopicData(2, "Từ vựng IELTS - Môi trường", 48),
        TopicData(3, "Công thức Lượng giác", 22),
        TopicData(4, "Khởi tạo Project Flutter", 5)
    )
}