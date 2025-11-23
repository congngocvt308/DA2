package com.example.myapplication.ui.theme.topic

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.QuestionData
import com.example.myapplication.data.TopicData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TopicDetailViewModel : ViewModel() {

    // --- 1. QUẢN LÝ DANH SÁCH CÂU HỎI (Màn hình chính) ---

    // Dữ liệu giả lập (Sau này thay bằng lấy từ Room DB)
    private val _questions = MutableStateFlow<List<QuestionData>>(
        listOf(
            QuestionData(1, "Ai là triệu phú?", listOf(AnswerData(1, "Tôi", true), AnswerData(2, "Bạn", false))),
            QuestionData(2, "1 + 1 = ?", listOf(AnswerData(3, "2", true), AnswerData(4, "3", false)))
        )
    )
    val questions: StateFlow<List<QuestionData>> = _questions.asStateFlow()

    // Hàm load dữ liệu (gọi khi mở màn hình)
    fun loadQuestions(topicId: Int) {
        // TODO: Gọi Repository/DAO để lấy câu hỏi theo topicId
        // _questions.value = repository.getQuestionsByTopic(topicId)
        println("Loading questions for topic $topicId")
    }

    // Hàm xóa câu hỏi
    fun deleteQuestion(questionId: Int) {
        _questions.update { currentList ->
            currentList.filter { it.id != questionId }
        }
        // TODO: Gọi Repository để xóa trong DB
    }

    // --- 2. QUẢN LÝ DIALOG "THÊM CÂU HỎI" (Logic nhập liệu) ---

    // State: Nội dung câu hỏi đang nhập
    private val _draftQuestionText = MutableStateFlow("")
    val draftQuestionText = _draftQuestionText.asStateFlow()

    // State: Danh sách đáp án đang nhập
    private val _draftAnswers = MutableStateFlow(
        listOf(
            AnswerData(0, "", false),
            AnswerData(1, "", false)
        )
    )
    val draftAnswers = _draftAnswers.asStateFlow()

    // Action: Người dùng gõ text câu hỏi
    fun onQuestionTextChange(text: String) {
        _draftQuestionText.value = text
    }

    // Action: Người dùng gõ text đáp án
    fun onAnswerTextChange(index: Int, text: String) {
        _draftAnswers.update { list ->
            val newList = list.toMutableList()
            newList[index] = newList[index].copy(text = text)
            newList
        }
    }

    // Action: Chọn đáp án đúng (Radio button)
    fun onSelectCorrectAnswer(selectedIndex: Int) {
        _draftAnswers.update { list ->
            list.mapIndexed { index, answer ->
                answer.copy(isCorrect = (index == selectedIndex))
            }
        }
    }

    // Action: Thêm dòng đáp án mới
    fun onAddAnswerLine() {
        _draftAnswers.update { list ->
            val newId = (list.maxOfOrNull { it.id } ?: 0) + 1
            list + AnswerData(newId, "", false)
        }
    }

    // Action: Xóa dòng đáp án
    fun onRemoveAnswerLine(index: Int) {
        _draftAnswers.update { list ->
            if (list.size > 1) {
                val newList = list.toMutableList()
                newList.removeAt(index)
                // Nếu xóa mất câu đúng, reset lại hoặc để trống
                newList
            } else {
                list // Giữ lại ít nhất 1 đáp án
            }
        }
    }

    // Action: LƯU CÂU HỎI MỚI
    fun saveNewQuestion() {
        // Tạo đối tượng mới
        val newQuestion = QuestionData(
            id = (System.currentTimeMillis() % 10000).toInt(), // ID giả
            questionText = _draftQuestionText.value,
            answers = _draftAnswers.value
        )

        // 1. Thêm vào danh sách hiển thị
        _questions.update { it + newQuestion }

        // 2. TODO: Lưu vào DB (repository.insert(newQuestion))

        // 3. Reset form để nhập câu tiếp theo
        resetDraft()
    }

    // Action: Reset form về mặc định
    fun resetDraft() {
        _draftQuestionText.value = ""
        _draftAnswers.value = listOf(
            AnswerData(0, "", false),
            AnswerData(1, "", false)
        )
    }

    val searchQuery = mutableStateOf("")
    val filteredQuestions: StateFlow<List<QuestionData>> = combine(
        _questions,
        snapshotFlow{ searchQuery.value }
    ){ questions, query ->
        if (query.isBlank()) questions
        else {
            questions.filter { question ->
                question.questionText.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}