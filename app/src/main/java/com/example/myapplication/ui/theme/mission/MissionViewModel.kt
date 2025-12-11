package com.example.myapplication.ui.theme.mission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.MissionQuestion
import com.example.myapplication.data.MissionTopic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MissionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.Companion.getDatabase(application).appDao()

    private val _missionTopics = MutableStateFlow<List<MissionTopic>>(emptyList())
    val missionTopics: StateFlow<List<MissionTopic>> = _missionTopics.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            dao.getTopicsWithQuestions().collect { dbList ->
                val uiList = dbList.map { item ->
                    MissionTopic(
                        id = item.topic.topicId,
                        name = item.topic.topicName,
                        questions = item.questions.map { q ->
                            MissionQuestion(
                                id = q.questionId.toString(),
                                text = q.prompt,
                                isSelected = false
                            )
                        },
                        isExpanded = false,
                        isSelected = false
                    )
                }
                _missionTopics.value = uiList
            }
        }
    }
}