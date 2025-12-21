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

    companion object {
        // ID đặc biệt cho chủ đề mặc định (dùng số âm để không trùng với ID từ database)
        const val DEFAULT_TOPIC_ID = -999
        
        // Các câu hỏi mặc định
        val defaultQuestions = listOf(
            MissionQuestion(id = "default_1", text = "Tác phẩm nào KHÔNG thuộc Tứ đại danh tác?"),
            MissionQuestion(id = "default_2", text = "1 + 1 = ?"),
            MissionQuestion(id = "default_3", text = "Thủ đô Việt Nam?"),
            MissionQuestion(id = "default_4", text = "2 x 2 = ?"),
            MissionQuestion(id = "default_5", text = "Loại hình MVVM?")
        )
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            dao.getTopicsWithQuestions().collect { dbList ->
                // Tạo chủ đề mặc định
                val defaultTopic = MissionTopic(
                    id = DEFAULT_TOPIC_ID,
                    name = "Câu hỏi mặc định",
                    questions = defaultQuestions.map { it.copy(isSelected = false) },
                    isExpanded = false,
                    isSelected = false
                )
                
                // Chuyển đổi các chủ đề từ database
                val userTopics = dbList.map { item ->
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
                
                // Kết hợp chủ đề mặc định và chủ đề người dùng
                _missionTopics.value = listOf(defaultTopic) + userTopics
            }
        }
    }
}