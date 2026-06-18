package com.example.myapplication.ui.theme.topic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AiApiService
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.GenerateQuestionsRequest
import com.example.myapplication.data.QuestionData
import com.example.myapplication.data.QuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.java
import org.json.JSONObject
import org.json.JSONArray

class TopicDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val topicDao = AppDatabase.getDatabase(application).appDao()
    private val topicId: Int = savedStateHandle.get<Int>("topicId") ?: -1

    private val _isStreamingActive = MutableStateFlow(false)
    val isStreamingActive = _isStreamingActive.asStateFlow()

    private val _streamNotificationMessage = MutableStateFlow<String?>(null)
    val streamNotificationMessage = _streamNotificationMessage.asStateFlow()

    private val _dbQuestions = MutableStateFlow<List<QuestionData>>(emptyList())
    val searchQuery = MutableStateFlow("")
    private val _topicName = MutableStateFlow("")
    val topicName = _topicName.asStateFlow()
    val filteredQuestions: StateFlow<List<QuestionData>> = combine(
        _dbQuestions,
        searchQuery
    ) { questions, query ->
        if (query.isBlank()) questions
        else questions.filter { it.questionText.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val questions = _dbQuestions.asStateFlow()

    init {
        if (topicId != -1) {
            loadQuestions()
            loadTopicInfo()

            // KIỂM TRA VÀ CHẠY STREAMING TỰ ĐỘNG
            TopicStreamBuffer.pendingRequest?.let { request ->
                if (request.topicId == topicId) {
                    startQuestionsStreaming(
                        request.extractedText,
                        request.easy,
                        request.mid,
                        request.hard
                    )
                    // Xóa dữ liệu đệm sau khi đã dùng để tránh chạy lại khi xoay màn hình
                    TopicStreamBuffer.pendingRequest = null
                }
            }
        }
    }

    private fun loadTopicInfo() {
        viewModelScope.launch {
            val name = topicDao.getTopicNameById(topicId)
            if (name != null) {
                _topicName.value = name
            }
        }
    }

    fun renameTopic(newName: String) {
        viewModelScope.launch {
            topicDao.updateTopicName(topicId, newName)
            _topicName.value = newName
        }
    }

    fun deleteCurrentTopic(onDeleted: () -> Unit) {
        viewModelScope.launch {
            topicDao.deleteTopicById(topicId)
            onDeleted()
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            topicDao.getQuestionsByTopic(topicId).collect { entities ->
                val mappedList = entities.map { entity ->
                    val correctAns = AnswerData(0, entity.correctAnswer, true)
                    val wrongAns = entity.options.mapIndexed { index, text ->
                        AnswerData(index + 1, text, false)
                    }
                    val allAnswers = (listOf(correctAns) + wrongAns).shuffled()
                    QuestionData(id = entity.questionId, questionText = entity.prompt, answers = allAnswers)
                }
                _dbQuestions.value = mappedList
            }
        }
    }

    fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            val questionEntity = topicDao.getQuestionById(questionId)
            if (questionEntity != null) {
                topicDao.deleteQuestion(questionEntity)
            }
        }
    }

    private var editingQuestionId: Int = -1

    private val _draftQuestionText = MutableStateFlow("")
    val draftQuestionText = _draftQuestionText.asStateFlow()

    private val _draftAnswers = MutableStateFlow(
        listOf(AnswerData(0, "", false), AnswerData(1, "", true))
    )

    val draftAnswers = _draftAnswers.asStateFlow()

    fun onQuestionTextChange(text: String) { _draftQuestionText.value = text }

    fun onAnswerTextChange(index: Int, text: String) {
        _draftAnswers.update { list ->
            val newList = list.toMutableList()
            newList[index] = newList[index].copy(text = text)
            newList
        }
    }

    fun onSelectCorrectAnswer(selectedIndex: Int) {
        _draftAnswers.update { list ->
            list.mapIndexed { index, answer ->
                answer.copy(isCorrect = (index == selectedIndex))
            }
        }
    }

    fun onAddAnswerLine() {
        _draftAnswers.update { list ->
            if (list.size < 5) {
                val newId = (list.maxOfOrNull { it.id } ?: 0) + 1
                list + AnswerData(newId, "", false)
            } else {
                list
            }
        }
    }

    fun onRemoveAnswerLine(index: Int) {
        _draftAnswers.update { list ->
            if (list.size > 1) {
                val newList = list.toMutableList()
                newList.removeAt(index)
                newList
            } else {
                list
            }
        }
    }

    fun resetDraft() {
        editingQuestionId = -1
        _draftQuestionText.value = ""
        _draftAnswers.value = listOf(
            AnswerData(0, "", false),
            AnswerData(1, "", true)
        )
    }

    fun startEditing(question: QuestionData) {
        editingQuestionId = question.id
        _draftQuestionText.value = question.questionText
        _draftAnswers.value = question.answers
    }

    fun saveQuestion() {
        viewModelScope.launch {
            val answers = _draftAnswers.value.filter { it.text.isNotBlank() }
            if (answers.isEmpty() || topicId == -1) return@launch

            val correctIndex = answers.indexOfFirst { it.isCorrect }.let {
                if (it == -1) answers.indices.random() else it
            }
            val questionEntity = QuestionEntity(
                questionId = if (editingQuestionId == -1) 0 else editingQuestionId,
                ownerTopicId = topicId,
                prompt = _draftQuestionText.value,
                correctAnswer = answers[correctIndex].text,
                options = answers.filterIndexed { i, _ -> i != correctIndex }.map { it.text }
            )
            if (editingQuestionId == -1) {
                topicDao.insertQuestion(questionEntity)
            } else {
                topicDao.updateQuestion(questionEntity)
            }
            resetDraft()
            loadQuestions()
        }
    }

    fun startQuestionsStreaming(extractedText: String, easy: Int, mid: Int, hard: Int) {
        if (topicId == -1 || extractedText.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isStreamingActive.value = true
            _streamNotificationMessage.value = "AI đang bắt đầu tạo câu hỏi toán học..."

            try {
                // 1. Khởi tạo OkHttpClient kiên nhẫn tăng timeout bảo vệ luồng dữ liệu dài
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.1.219:3000/") // Khớp với IP mạng Wi-Fi Local của bạn
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val aiApiService = retrofit.create(AiApiService::class.java)

                // 2. Bắn cấu hình Slider và chuỗi đệm văn bản lên Endpoint hạ nguồn
                val response = aiApiService.generateQuestionsStream(
                    GenerateQuestionsRequest(
                        extractedText = extractedText,
                        easyCount = easy,
                        midCount = mid,
                        hardCount = hard
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val inputStream = response.body()!!.byteStream()
                    val reader = inputStream.bufferedReader()

                    // 🌟 BỘ ĐỆM THẦN THÁNH: Tách biệt kho lưu trữ chuỗi cho từng câu hỏi
                    // Key: questionIndex (Int) | Value: StringBuilder (Cộng dồn token)
                    val streamBuffers = HashMap<Int, StringBuilder>()

                    reader.useLines { lines ->
                        lines.forEach { line ->
                            if (line.startsWith("data:")) {
                                val dataContent = line.substring(5).trim()

                                if (dataContent == "[DONE]") {
                                    Log.d("AiOcrTest", "✅ AI đã truyền tải xong 100% bộ đề.")
                                    viewModelScope.launch(Dispatchers.Main) {
                                        _streamNotificationMessage.value = "🎉 Hoàn thành! Bộ đề toán đã được AI tạo sinh xong vẹn toàn."
                                    }
                                } else if (dataContent.isNotBlank()) {
                                    try {
                                        // 1. Parse cái bọc bưu kiện mạng chứa token ra trước
                                        val networkPayload = JSONObject(dataContent)

                                        // Kiểm tra nếu packet chứa lỗi từ hệ thống phía sau
                                        if (networkPayload.has("error")) {
                                            Log.e("AiOcrTest", "❌ Lỗi phân đoạn từ câu hỏi: ${networkPayload.getString("error")}")
                                            return@forEach
                                        }

                                        val qIndex = networkPayload.getInt("questionIndex")
                                        val rawToken = networkPayload.getString("token")

                                        // 2. Lấy bộ đệm tương ứng của câu hỏi này ra để cộng dồn chữ
                                        if (!streamBuffers.containsKey(qIndex)) {
                                            streamBuffers[qIndex] = StringBuilder()
                                        }
                                        streamBuffers[qIndex]?.append(rawToken)

                                        val currentFullText = streamBuffers[qIndex].toString().trim()

                                        // 3. KIỂM TRA ĐIỀU KIỆN ĐÓNG: Nếu chuỗi đã kết thúc bằng dấu vạch đóng JSON '}'
                                        if (currentFullText.endsWith("}")) {
                                            // ... Bên trong hàm startQuestionsStreaming, khúc currentFullText.endsWith("}") ...
                                            try {
                                                val qJson = JSONObject(currentFullText)
                                                val prompt = qJson.getString("question")

                                                // 1. Đọc Object options dạng {"A": "Đáp án 1", "B": "Đáp án 2", "C": "...", "D": "..."}
                                                val optionsObj = qJson.getJSONObject("options")

                                                // 2. Lấy chữ cái đại diện đáp án đúng từ AI (Ví dụ: "A") rồi chuẩn hóa viết hoa
                                                val correctKey = qJson.getString("correct_answer").trim().uppercase()

                                                // 3. 🌟 BƯỚC KHỚP NỐI: Lấy nội dung TEXT THỰC TẾ bên trong chữ cái đó
                                                // Nếu correctKey là "A", realCorrectAnswerText sẽ lấy giá trị của optionsObj.getString("A") (Ví dụ: "2")
                                                val realCorrectAnswerText = if (optionsObj.has(correctKey)) {
                                                    optionsObj.getString(correctKey)
                                                } else {
                                                    // Phương án phòng vệ nếu AI lỡ tay viết text trực tiếp thay vì chữ cái đại diện
                                                    qJson.getString("correct_answer")
                                                }

                                                // 4. 🌟 BƯỚC LỌC ĐÁP ÁN SAI: Duyệt qua A, B, C, D, cái nào KHÔNG PHẢI correctKey thì mới cho vào mảng options (đáp án sai)
                                                val wrongOptionsList = mutableListOf<String>()
                                                val keys = optionsObj.keys()
                                                while (keys.hasNext()) {
                                                    val key = keys.next()
                                                    if (key.trim().uppercase() != correctKey) {
                                                        wrongOptionsList.add(optionsObj.getString(key))
                                                    }
                                                }

                                                // 5. Đóng gói Entity chuẩn chỉ để ghi xuống Room
                                                val finalQuestion = QuestionEntity(
                                                    questionId = 0, // Room tự tăng
                                                    ownerTopicId = topicId,
                                                    prompt = prompt,
                                                    correctAnswer = realCorrectAnswerText, // Bây giờ đã lưu TEXT THẬT (Ví dụ: "2"), không còn bị chữ "A" nữa
                                                    options = wrongOptionsList // Chỉ chứa đúng 3 đáp án sai còn lại (Ví dụ: "3", "4", "5")
                                                )

                                                // Tiến hành Insert ngay vào DB để Room Flow bắn lên UI cập nhật thời gian thực
                                                viewModelScope.launch(Dispatchers.IO) {
                                                    topicDao.insertQuestion(finalQuestion)
                                                }

                                                // Giải phóng bộ đệm của câu hỏi này
                                                streamBuffers.remove(qIndex)
                                                Log.d("AiOcrTest", "🎯 Đã kết tinh thành công câu hỏi chuẩn 4 lựa chọn cho câu: $qIndex")

                                            } catch (e: Exception) {
                                                // Ép dồn chữ tiếp nếu JSON chưa map đủ cấu trúc ngoặc
                                            }
                                        }

                                    } catch (e: Exception) {
                                        Log.e("AiOcrTest", "⚠️ Mảnh vỡ packet lỗi: ${e.localizedMessage}")
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("AiOcrTest", "❌ Lỗi kết nối luồng Stream: ${e.localizedMessage}")
                _streamNotificationMessage.value = "❌ Mất kết nối đường truyền mạng."
            } finally {
                _isStreamingActive.value = false
                // Tải lại danh sách từ DB để bảo đảm hiển thị đầy đủ
                loadQuestions()
            }
        }
    }

    fun clearStreamNotification() {
        _streamNotificationMessage.value = null
    }

}