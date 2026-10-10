package com.cong.smartalarm.feature.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.cong.smartalarm.SmartAlarmApp
import com.cong.smartalarm.data.repository.TopicRepository
import com.cong.smartalarm.feature.mission.MissionQuestion
import com.cong.smartalarm.feature.mission.MissionTopic
import com.cong.smartalarm.feature.mission.MissionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MissionViewModel(
    private val topicRepository: TopicRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MissionUiState())
    val uiState: StateFlow<MissionUiState> = _uiState.asStateFlow()

    companion object {
        const val DEFAULT_TOPIC_ID = -999
        val defaultQuestions = listOf(
            MissionQuestion(id = -1, text = "1 + 1 = ?", isSelected = false),
            MissionQuestion(id = -2, text = "Thủ đô Việt Nam?", isSelected = false),
            MissionQuestion(id = -3, text = "2 x 2 = ?", isSelected = false)
        )

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartAlarmApp
                MissionViewModel(app.container.topicRepository)
            }
        }
    }

    // Khởi tạo dữ liệu lần đầu
    fun initData(initialCount: Int, selectedIds: Set<Int>, initialTopicIds: Set<Int>) {
        viewModelScope.launch {
            topicRepository.getTopicsWithQuestions().collect { dbList ->
                val allTopics = mutableListOf<MissionTopic>()

                // 1. Thêm Topic mặc định
                val defQuestions = defaultQuestions.map { it.copy(isSelected = selectedIds.contains(it.id)) }
                allTopics.add(MissionTopic(
                    id = DEFAULT_TOPIC_ID,
                    name = "Câu hỏi mặc định",
                    questions = defQuestions,
                    isSelected = defQuestions.all { it.isSelected },
                    isExpanded = defQuestions.any { it.isSelected }
                ))

                // 2. Thêm Topic từ DB
                val userTopics = dbList.map { item ->
                    val isTopicFullSelected = initialTopicIds.contains(item.topic.topicId)
                    val qs = item.questions.map { q ->
                        MissionQuestion(
                            id = q.questionId,
                            text = q.prompt,
                            // Logic quan trọng: Câu hỏi được chọn nếu ID nó có trong SelectedIds
                            // HOẶC Topic cha của nó được chọn Full
                            isSelected = isTopicFullSelected || selectedIds.contains(q.questionId)
                        )
                    }


                    MissionTopic(
                        id = item.topic.topicId,
                        name = item.topic.topicName,
                        questions = qs,
                        isSelected = isTopicFullSelected || (qs.isNotEmpty() && qs.all { it.isSelected }),
                        isExpanded = qs.any { it.isSelected } || isTopicFullSelected
                    )
                }
                allTopics.addAll(userTopics)

                _uiState.update { it.copy(topics = allTopics, questionCount = initialCount, isLoading = false) }
            }
        }
    }

    fun toggleQuestion(topicId: Int, questionId: Int) {
        _uiState.update { state ->
            val newTopics = state.topics.map { topic ->
                if (topic.id == topicId) {
                    val newQs = topic.questions.map { if (it.id == questionId) it.copy(isSelected = !it.isSelected) else it }
                    topic.copy(questions = newQs, isSelected = newQs.all { it.isSelected })
                } else topic
            }
            state.copy(topics = newTopics)
        }
    }

    fun toggleTopic(topicId: Int, selectAll: Boolean) {
        _uiState.update { state ->
            val newTopics = state.topics.map { topic ->
                if (topic.id == topicId) {
                    val newQs = topic.questions.map { it.copy(isSelected = selectAll) }
                    topic.copy(questions = newQs, isSelected = selectAll)
                } else topic
            }
            state.copy(topics = newTopics)
        }
    }

    fun toggleExpansion(topicId: Int) {
        _uiState.update { state ->
            val newTopics = state.topics.map { if (it.id == topicId) it.copy(isExpanded = !it.isExpanded) else it }
            state.copy(topics = newTopics)
        }
    }

    fun updateCount(count: Int) {
        _uiState.update { it.copy(questionCount = count) }
    }
}