package com.example.myapplication.ui.theme.topic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.TopicData
import com.example.myapplication.data.TopicEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val topicDao = AppDatabase.getDatabase(application).appDao()

    val searchQuery = MutableStateFlow("")

    val filteredTopics: StateFlow<List<TopicData>> = combine(
        topicDao.getAllTopicsWithCount(),
        searchQuery
    ) { entities, query ->
        val mappedTopics = entities.map { entity ->
            TopicData(
                id = entity.topicId,
                name = entity.topicName,
                questionCount = entity.questionCount
            )
        }
        if (query.isBlank()) {
            mappedTopics
        } else {
            mappedTopics.filter { topic ->
                topic.name.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun addNewTopic(name: String) {
        viewModelScope.launch {
            val newTopic = TopicEntity(topicName = name)
            topicDao.insertTopic(newTopic)
        }
    }
}