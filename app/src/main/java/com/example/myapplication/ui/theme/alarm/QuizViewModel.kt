package com.example.myapplication.ui.theme.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AlarmHistoryEntity
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.QuestionData
import com.example.myapplication.data.QuizUiStateData
// 1. IMPORT CLASS THUẬT TOÁN
import com.example.myapplication.logic.QuestionAlgorithmManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

const val QUESTION_DURATION_MS = 15000L
const val TICK_INTERVAL_MS = 50L

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(QuizUiStateData())
    val uiState: StateFlow<QuizUiStateData> = _uiState.asStateFlow()
    private var timerJob: Job? = null
    private val dao = AppDatabase.getDatabase(application).appDao()

    // 2. KHỞI TẠO BỘ NÃO THUẬT TOÁN
    private val algorithmManager = QuestionAlgorithmManager(dao)

    private var alarmId: Int = -1

    private var currentAlarmHistoryId: Int? = null

    fun setAlarmId(id: Int) {
        alarmId = id
        loadQuestionsForQuiz()
    }

    private fun loadQuestionsForQuiz() {
        viewModelScope.launch {
            // Cập nhật trạng thái đang tải (cần thêm isLoading vào QuizUiStateData nếu chưa có)
            _uiState.update { it.copy(isLoading = true) }

            // BƯỚC A: KIỂM TRA SỐ LƯỢNG CÂU HỎI NGƯỜI DÙNG CÀI ĐẶT
            val alarm = if (alarmId != -1) dao.getAlarmById(alarmId) else null
            val targetCount = alarm?.questionCount ?: 0 // Mặc định 3 nếu không tìm thấy

            // Nếu người dùng chọn 0 câu hỏi -> Tắt báo thức ngay lập tức
            if (targetCount == 0) {
                _uiState.update {
                    it.copy(
                        isFinished = true,
                        questionPool = emptyList()
                    )
                }
                return@launch
            }

            if (alarmId != -1) {
                val newHistory = AlarmHistoryEntity(
                    alarmId = alarmId,
                    snoozeCount = 0,
                    scheduledTime = Date(), // Thời gian báo thức reo
                    firstRingTime = Date(),
                    dismissalTime = null,
                    isDismissed = false
                )
                // Insert và lấy ID về để dùng cập nhật sau này
                currentAlarmHistoryId = dao.insertAlarmHistory(newHistory).toInt()
            }

            // BƯỚC B: GỌI THUẬT TOÁN ĐỂ LẤY DANH SÁCH CÂU HỎI (SRS)
            val generatedQuestions = try {
                if (alarmId != -1) {
                    // Thuật toán sẽ tự lọc câu hỏi, sắp xếp theo độ ưu tiên
                    algorithmManager.generateMissionQuestions(alarmId, targetCount)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }

            // Fallback: Nếu không lấy được câu nào (lỗi mạng/DB), dùng Mock
            if (generatedQuestions.isEmpty()) {
                loadMockQuestions(targetCount)
                return@launch
            }

            // BƯỚC C: CHUYỂN ĐỔI DỮ LIỆU (MissionQuestion -> QuestionData có đáp án)
            val fullQuestions = mutableListOf<QuestionData>()
            var answerIdCounter = 0

            generatedQuestions.forEach { missionQ ->
                if (missionQ.id < 0) {
                    // Xử lý câu hỏi mặc định (ID âm)
                    // Lưu ý: missionQ.id là số âm (-1), hàm get của bạn đang case 1, 2...
                    // nên cần lấy abs()
                    getDefaultQuestionById(abs(missionQ.id))?.let { qData ->
                        fullQuestions.add(qData.copy(
                            answers = qData.answers.map {
                                it.copy(id = answerIdCounter++)
                            }.shuffled()
                        ))
                    }
                } else {
                    // Xử lý câu hỏi từ Database (ID dương)
                    val entity = dao.getQuestionById(missionQ.id)
                    if (entity != null) {
                        val correctAns = AnswerData(answerIdCounter++, entity.correctAnswer, true)
                        // Tách chuỗi options (giả sử đang lưu dạng JSON String hoặc String thường)
                        // Bạn cần đảm bảo entity.options trả về List<String>.
                        // Nếu entity.options là String, hãy dùng Gson để parse.
                        val wrongAnswers = entity.options.map { text ->
                            AnswerData(answerIdCounter++, text, false)
                        }
                        val allAnswers = (listOf(correctAns) + wrongAnswers).shuffled()

                        fullQuestions.add(QuestionData(
                            id = entity.questionId,
                            questionText = entity.prompt,
                            answers = allAnswers
                        ))
                    }
                }
            }

            // Nếu convert xong mà vẫn rỗng -> dùng Mock
            if (fullQuestions.isEmpty()) {
                loadMockQuestions(targetCount)
                return@launch
            }

            // Cập nhật UI và bắt đầu đếm giờ
            _uiState.value = QuizUiStateData(
                questionPool = fullQuestions, // Không cần shuffle nữa vì thuật toán đã sắp xếp rồi
                poolIndex = 0,
                targetCorrectAnswers = fullQuestions.size,
                timerProgress = 1f,
                correctlyAnsweredCount = 0,
                isLoading = false
            )
            startTimer()
        }
    }

    // Giữ nguyên hàm này của bạn
    private fun getDefaultQuestionById(id: Int): QuestionData? {
        return when (id) {
            1 -> QuestionData(-1, "Tác phẩm nào KHÔNG thuộc Tứ đại danh tác?", listOf(
                AnswerData(0, "Hồng Lâu Mộng", false), AnswerData(1, "Liêu Trai Chí Dị", true),
                AnswerData(2, "Tam Quốc Diễn Nghĩa", false), AnswerData(3, "Thủy Hử", false)
            ))
            2 -> QuestionData(-2, "1 + 1 = ?", listOf(
                AnswerData(0, "3", false), AnswerData(1, "2", true),
                AnswerData(2, "1", false), AnswerData(3, "0", false)
            ))
            3 -> QuestionData(-3, "Thủ đô Việt Nam?", listOf(
                AnswerData(0, "Hà Nội", true), AnswerData(1, "Hồ Chí Minh", false),
                AnswerData(2, "Đà Nẵng", false), AnswerData(3, "Huế", false)
            ))
            else -> null
        }
    }

    private fun loadMockQuestions(count: Int = 5) {
        val allMocks = listOf(
            QuestionData(1, "Tác phẩm nào KHÔNG thuộc Tứ đại danh tác?", listOf(AnswerData(1, "Hồng Lâu Mộng", false), AnswerData(2, "Liêu Trai Chí Dị", true))),
            QuestionData(2, "1 + 1 = ?", listOf(AnswerData(3, "3", false), AnswerData(4, "2", true))),
            QuestionData(3, "Thủ đô Việt Nam?", listOf(AnswerData(5, "Hà Nội", true), AnswerData(6, "Hồ Chí Minh", false))),
        )
        // Chỉ lấy đúng số lượng cần thiết
        val finalMocks = allMocks.take(count)

        _uiState.value = QuizUiStateData(
            questionPool = finalMocks,
            poolIndex = 0,
            targetCorrectAnswers = finalMocks.size,
            timerProgress = 1f,
            correctlyAnsweredCount = 0,
            isLoading = false
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var timeLeft = QUESTION_DURATION_MS
            _uiState.update { it.copy(timerProgress = 1f, isTimeOut = false) }

            while (timeLeft > 0) {
                delay(TICK_INTERVAL_MS)
                timeLeft -= TICK_INTERVAL_MS
                val progress = timeLeft.toFloat() / QUESTION_DURATION_MS
                _uiState.update { it.copy(timerProgress = progress) }
            }
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

    // Logic khi người dùng chọn đáp án (ĐÃ UPDATE SRS)
    fun onOptionSelected(submittedAnswerId: Int) {
        val currentState = _uiState.value
        if (currentState.isAnswered) return
        timerJob?.cancel()

        val currentQuestion = currentState.questionPool.getOrNull(currentState.poolIndex) ?: return
        val isCorrect = currentQuestion.answers.find { it.id == submittedAnswerId }?.isCorrect ?: false

        // 1. Cập nhật UI ngay lập tức
        _uiState.update {
            it.copy(
                selectedAnswerId = submittedAnswerId,
                isAnswered = true,
            )
        }

        // 2. TÍNH TOÁN VÀ LƯU KẾT QUẢ VÀO THUẬT TOÁN (Quan trọng)
        // Chỉ lưu nếu là câu hỏi thật (ID > 0), bỏ qua câu mặc định (ID < 0)
        if (currentQuestion.id > 0) {
            viewModelScope.launch {
                // Tính thời gian đã trả lời (dựa trên thanh progress)
                val timeSpent = ((1f - currentState.timerProgress) * QUESTION_DURATION_MS).toLong()

                algorithmManager.processAnswer(
                    questionId = currentQuestion.id,
                    isCorrect = isCorrect,
                    timeSpentMs = timeSpent,
                    alarmHistoryId = currentAlarmHistoryId // Nếu bạn có lưu lịch sử Alarm thì truyền ID vào đây
                )
            }
        }

        // 3. Chuyển câu hỏi sau 1 giây
        viewModelScope.launch {
            delay(1000)
            nextQuestion(isCorrect)
        }
    }

    private fun nextQuestion(wasCorrect: Boolean) {
        _uiState.update { state ->
            val newCorrectCount = state.correctlyAnsweredCount + if (wasCorrect) 1 else 0

            if (newCorrectCount >= state.targetCorrectAnswers) {
                finishAlarmHistory()
                state.copy(isFinished = true)
            } else {
                val nextPoolIndex = (state.poolIndex + 1) % state.questionPool.size

                state.copy(
                    poolIndex = nextPoolIndex,
                    correctlyAnsweredCount = newCorrectCount,
                    selectedAnswerId = null,
                    isAnswered = false,
                    isTimeOut = false
                )
            }
        }

        if (!_uiState.value.isFinished) {
            startTimer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private fun finishAlarmHistory() {
        if (currentAlarmHistoryId != null) {
            viewModelScope.launch {
                val history = dao.getAlarmHistoryById(currentAlarmHistoryId!!)
                if (history != null) {
                    dao.updateAlarmHistory(
                        history.copy(
                            dismissalTime = java.util.Date(), // Ghi nhận thời gian tắt
                            isDismissed = true
                        )
                    )
                }
            }
        }
    }
}