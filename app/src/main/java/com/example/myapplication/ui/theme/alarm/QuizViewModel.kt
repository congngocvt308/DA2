package com.example.myapplication.ui.theme.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.QuestionData
import com.example.myapplication.data.QuizUiStateData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val QUESTION_DURATION_MS = 15000L
const val TICK_INTERVAL_MS = 50L

class QuizViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiStateData())
    val uiState: StateFlow<QuizUiStateData> = _uiState.asStateFlow()
    private var timerJob: Job? = null

    init {
        loadQuestionsForQuiz()
    }

    private fun loadQuestionsForQuiz() {
        // Giả lập data 5 câu hỏi (Trong thực tế nên có nhiều hơn 5)
        val mockQuestions = listOf(
            QuestionData(1, "Tác phẩm nào KHÔNG thuộc Tứ đại danh tác?", listOf(AnswerData(1, "Hồng Lâu Mộng", false), AnswerData(2, "Liêu Trai Chí Dị", true))),
            QuestionData(2, "1 + 1 = ?", listOf(AnswerData(3, "3", false), AnswerData(4, "2", true))),
            QuestionData(3, "Thủ đô Việt Nam?", listOf(AnswerData(5, "Hà Nội", true), AnswerData(6, "Hồ Chí Minh", false))),
            QuestionData(4, "2 x 2 = ?", listOf(AnswerData(7, "5", false), AnswerData(8, "4", true))),
            QuestionData(5, "Loại hình MVVM?", listOf(AnswerData(9, "MVC", false), AnswerData(10, "MVVM", true)))
        )
        _uiState.value = QuizUiStateData(
            questionPool = mockQuestions,
            poolIndex = 0,
            targetCorrectAnswers = 5,
            timerProgress = 1f,           // Thanh thời gian đầy
            correctlyAnsweredCount = 0    // Chưa đúng câu nào
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel() // Hủy timer cũ
        timerJob = viewModelScope.launch {
            var timeLeft = QUESTION_DURATION_MS

            // Reset trạng thái timer
            _uiState.update { it.copy(timerProgress = 1f, isTimeOut = false) }

            while (timeLeft > 0) {
                delay(TICK_INTERVAL_MS)
                timeLeft -= TICK_INTERVAL_MS
                val progress = timeLeft.toFloat() / QUESTION_DURATION_MS
                _uiState.update { it.copy(timerProgress = progress) }
            }

            // Hết giờ -> Báo TimeOut
            handleTimeOut()
        }
    }

    private fun handleTimeOut() {
        _uiState.update { state ->
            state.copy(
                isTimeOut = true,
                correctlyAnsweredCount = 0,
                poolIndex = (state.poolIndex + 1) % state.questionPool.size,
                selectedAnswerId = null,
                isAnswered = false
            )
        }
        viewModelScope.launch {
            delay(500)
            startTimer()
        }
    }

    // Logic khi người dùng chọn đáp án
    fun onOptionSelected(submittedAnswerId: Int) {
        val currentState = _uiState.value
        if (currentState.isAnswered) return // Chặn nếu đã có phản hồi
        timerJob?.cancel()

        val currentQuestion = currentState.questionPool.getOrNull(currentState.poolIndex) ?: return
        val isCorrect = currentQuestion.answers.find { it.id == submittedAnswerId }?.isCorrect ?: false

        // 1. Cập nhật state để UI hiển thị màu sắc phản hồi (Đỏ/Xanh)
        _uiState.update {
            it.copy(
                selectedAnswerId = submittedAnswerId,
                isAnswered = true, // Bật cờ phản hồi màu
            )
        }

        // 2. LOGIC CHUYỂN CÂU HỎI SAU 1 GIÂY (Bất kể đúng/sai)
        viewModelScope.launch {
            delay(1000) // Chờ 1 giây để người dùng xem màu đỏ/xanh
            nextQuestion(isCorrect)
        }
    }
    private fun nextQuestion(wasCorrect: Boolean) {
        _uiState.update { state ->
            // Chỉ tăng điểm nếu trả lời ĐÚNG
            val newCorrectCount = state.correctlyAnsweredCount + if (wasCorrect) 1 else 0

            // Kiểm tra điều kiện thắng (Đủ 5 câu đúng)
            if (newCorrectCount >= state.targetCorrectAnswers) {
                state.copy(isFinished = true)
            } else {
                // Luôn chuyển sang câu hỏi tiếp theo trong Pool
                // Dùng % (modulo) để quay vòng nếu hết câu hỏi trong kho
                val nextPoolIndex = (state.poolIndex + 1) % state.questionPool.size

                state.copy(
                    poolIndex = nextPoolIndex,
                    correctlyAnsweredCount = newCorrectCount, // Cập nhật điểm
                    selectedAnswerId = null,                  // Reset lựa chọn
                    isAnswered = false,                       // Reset màu sắc
                    isTimeOut = false                         // Reset timeout
                )
            }
        }

        // Nếu chưa xong game -> Chạy lại timer cho câu mới
        if (!_uiState.value.isFinished) {
            startTimer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}