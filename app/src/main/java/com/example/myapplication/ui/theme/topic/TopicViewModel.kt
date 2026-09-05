package com.cong.smartalarm.ui.theme.topic

import android.app.Application
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cong.smartalarm.data.AiApiService
import com.cong.smartalarm.data.AiMatrixConfig
import com.cong.smartalarm.data.AppDatabase
import com.cong.smartalarm.data.GenerateQuestionsRequest
import com.cong.smartalarm.data.TopicData
import com.cong.smartalarm.data.TopicEntity
import com.cong.smartalarm.data.SelectedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val topicDao = AppDatabase.getDatabase(application).appDao()
    private val context = application.applicationContext
    private val documentCompressor = DocumentCompressor(context)

    private val _streamingQuestions = MutableStateFlow<List<String>>(emptyList())
    val streamingQuestions: StateFlow<List<String>> = _streamingQuestions.asStateFlow()

    val searchQuery = MutableStateFlow("")

    // --- CÁC TRẠNG THÁI UI QUẢN LÝ TẬP TRUNG (STATE HOISTING) ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _matrixConfig = MutableStateFlow<AiMatrixConfig?>(null)
    val matrixConfig: StateFlow<AiMatrixConfig?> = _matrixConfig.asStateFlow()

    private val _selectedDocuments = MutableStateFlow<List<SelectedDocument>>(emptyList())
    val selectedDocuments: StateFlow<List<SelectedDocument>> = _selectedDocuments.asStateFlow()

    // Cache lưu trữ chuỗi văn bản toán học LaTeX tạm thời do Backend trả về sau Bước 1
    private val _extractedTextCache = MutableStateFlow("")

    // --- LUỒNG TÌM KIẾM CHỦ ĐỀ TRÊN THƯ VIỆN CÓ SẴN ---
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
        if (query.isBlank()) { mappedTopics } else {
            mappedTopics.filter { topic -> topic.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    // --- HÀNG CHỜ: THÊM TÀI LIỆU CÓ SÀNG LỌC GUARDRAIL ---
    fun addDocumentToQueue(uri: Uri, name: String, isPdf: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            var count = 1
            var isTextPdf = false

            if (isPdf) {
                try {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        val renderer = PdfRenderer(pfd)
                        count = renderer.pageCount
                        renderer.close()
                    }

                    val totalFileSize = getFileSize(uri)
                    val averagePageSize = totalFileSize.toFloat() / count.toFloat()
                    val maxTextPageSizeThreshold = 500 * 1024 // 500KB Guardrail Threshold

                    if (averagePageSize < maxTextPageSizeThreshold) {
                        isTextPdf = true
                    }

                    Log.d("AiOcrTest", "📊 HÀNG CHỜ - ĐO THÔNG SỐ FILE:")
                    Log.d("AiOcrTest", "   ➔ Tên: $name | Tổng số trang: $count trang")
                    Log.d("AiOcrTest", "   ➔ Kích thước trung bình trang: ${String.format("%.2f", averagePageSize / 1024.0)} KB/Trang")
                    Log.d("AiOcrTest", "   ➔ Phân loại -> PDF Text: $isTextPdf")
                } catch (e: Exception) {
                    Log.e("AiOcrTest", "❌ Lỗi đọc cấu trúc phân loại PDF: ${e.message}")
                }
            }

            val newDoc = SelectedDocument(
                id = java.util.UUID.randomUUID().toString(),
                uri = uri,
                name = name,
                isPdf = isPdf,
                isPdfText = isTextPdf,
                totalPages = count,
                pageConfig = "Tất cả"
            )
            _selectedDocuments.update { it + newDoc }
        }
    }

    fun removeDocumentFromQueue(id: String) {
        _selectedDocuments.update { list -> list.filter { it.id != id } }
        Log.d("AiOcrTest", "❌ Đã xóa tài liệu khỏi danh sách chờ.")
    }

    fun updateDocumentPageConfig(id: String, newPageConfig: String) {
        _selectedDocuments.update { list ->
            list.map { doc -> if (doc.id == id) doc.copy(pageConfig = newPageConfig) else doc }
        }
    }

    // ================= 🌟 THỰC THI TIẾN TRÌNH KIỂM THỬ BƯỚC 1 (WORKFLOW MỚI) =================
    fun processAndCompressQueue() {
        val currentDocs = _selectedDocuments.value
        if (currentDocs.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            // 1. Kích hoạt hiệu ứng loading
            _isLoading.value = true

            Log.d("AiOcrTest", "=========================================================================")
            Log.d("AiOcrTest", "⚡ KÍCH HOẠT TIẾN TRÌNH XỬ LÝ NÉN PHÂN NHÁNH VÀ KIỂM THỬ BƯỚC 1")
            Log.d("AiOcrTest", "Số lượng tài liệu trong hàng chờ chuẩn bị đẩy đi: ${currentDocs.size}")

            // 2. Chạy nén ngầm
            val startTime = System.currentTimeMillis()
            val compressedDataList = documentCompressor.compressMultipleDocuments(currentDocs)
            val endTime = System.currentTimeMillis()

            // 3. In log báo cáo nén
            Log.d("AiOcrTest", "----------- BẢNG TỔNG HỢP KẾT QUẢ NÉN TẠI MEMORY -----------")
            Log.d("AiOcrTest", "Thời gian thực thi nén bất đồng bộ: ${endTime - startTime} ms")
            var totalBytesToSend = 0L

            compressedDataList.forEachIndexed { index, fileData ->
                val sizeInKb = fileData.bytes.size / 1024
                totalBytesToSend += fileData.bytes.size
                Log.d("AiOcrTest", "📍 Mảnh [${index + 1}]: ${fileData.fileName} | Dung lượng Memory gửi: ${sizeInKb} KB | Định dạng gốc PDF Text: ${fileData.isRawPdf}")
            }
            Log.d("AiOcrTest", "Tổng băng thông truyền dữ liệu mạng Multipart dự kiến: ${totalBytesToSend / 1024} KB")
            Log.d("AiOcrTest", "------------------------------------------------------------")

            // 4. Truyền tải gói tin Multipart Stream lên Server
            Log.d("AiOcrTest", "📡 3G/4G/Wifi: Đang truyền tải gói tin Multipart Stream lên Server...")

            try {
                // 🌟 CHỈNH SỬA DÙNG EXTENSION FUNCTION CỦA OKHTTP CHUẨN MỚI
                val multipartParts = compressedDataList.map { fileData ->
                    val mimeType = if (fileData.isRawPdf) "application/pdf" else "image/jpeg"
                    val mediaType = mimeType.toMediaTypeOrNull()
                    val requestBody = fileData.bytes.toRequestBody(mediaType)

                    MultipartBody.Part.createFormData("files", fileData.fileName, requestBody)
                }

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

                val retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl("http://192.168.1.219:3000/")
                    .client(okHttpClient)
                    .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                    .build()

                val aiApiService = retrofit.create(AiApiService::class.java)

                // Bắn Request
                val response = aiApiService.uploadDocuments(multipartParts)

                if (response.isSuccessful && response.body() != null) {
                    val ocrResult = response.body()!!

                    Log.d("AiOcrTest", "✅ Backend Server phản hồi chuỗi cấu trúc JSON thành công!")
                    Log.d("AiOcrTest", "🤖 Tên chủ đề Gemini bóc tách xịn: -> '${ocrResult.suggestedTopic}'")

                    // 5. Trả dữ liệu về Main Thread
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        _extractedTextCache.value = ocrResult.extractedLatexContent ?: ""
                        Log.d("AiOcrTest", "📝 _extractedTextCache sau gán: '${_extractedTextCache.value}' (len=${_extractedTextCache.value.length})")

                        _matrixConfig.value = AiMatrixConfig(
                            suggestedTopic = ocrResult.suggestedTopic,
                            easyCount = 1f,
                            midCount = 3f,
                            hardCount = 2f
                        )
                    }
                } else {
                    Log.e("AiOcrTest", "❌ Server báo lỗi: ${response.code()} - ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) { _isLoading.value = false }
                }

            } catch (e: Exception) {
                Log.e("AiOcrTest", "❌ Lỗi kết nối API mạng: ${e.localizedMessage}")
                withContext(Dispatchers.Main) { _isLoading.value = false }
            }
        }
    }

    // --- CÁC SỰ KIỆN TƯƠNG TÁC FORM CẤU HÌNH GỘP (BƯỚC 2) ---
    fun updateTopicName(newName: String) {
        _matrixConfig.update { it?.copy(suggestedTopic = newName) }
    }

    fun applyPreset(isTryHard: Boolean) {
        _matrixConfig.update {
            if (isTryHard) {
                it?.copy(easyCount = 1f, midCount = 3f, hardCount = 2f)
            } else {
                it?.copy(easyCount = 3f, midCount = 1f, hardCount = 0f)
            }
        }
    }

    fun addNewTopic(name: String) {
        viewModelScope.launch {
            val newTopic = TopicEntity(topicName = name)
            topicDao.insertTopic(newTopic)
        }
    }

    fun updateMatrixSliders(easy: Float, mid: Float, hard: Float) {
        _matrixConfig.update { it?.copy(easyCount = easy, midCount = mid, hardCount = hard) }
    }

    fun finalizeAndGenerateQuestions(
        onSuccess: (topicId: Int, extractedText: String, easy: Int, mid: Int, hard: Int) -> Unit
    ) {
        val config = _matrixConfig.value ?: return
        val textToStream = _extractedTextCache.value.orEmpty()

        if (textToStream.isBlank()) {
            Log.e("AiOcrTest", "❌ Lỗi rào gác: Chuỗi đệm LaTeX rỗng, không thể sinh câu hỏi.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("AiOcrTest", "🚀 KHỞI ĐỘNG TIẾN TRÌNH TÁC ĐỘNG CSDL GÁC CỔNG")
            val cleanTopicName = config.suggestedTopic.trim()

            try {
                val currentTopicsList = topicDao.getAllTopicsWithCount().first()
                val existingTopic = currentTopicsList.find {
                    it.topicName.equals(cleanTopicName, ignoreCase = true)
                }

                val finalTopicId = if (existingTopic != null) {
                    Log.d("AiOcrTest", "   ➔ Chủ đề cũ tồn tại. Sử dụng ID: ${existingTopic.topicId}")
                    existingTopic.topicId
                } else {
                    val newEntity = TopicEntity(topicName = cleanTopicName)
                    val newId = topicDao.insertTopic(newEntity)
                    Log.d("AiOcrTest", "   ➔ Khởi tạo chủ đề mới. INSERT thành công dòng với ID: $newId")
                    newId.toInt()
                }

                val easy = config.easyCount.toInt()
                val mid = config.midCount.toInt()
                val hard = config.hardCount.toInt()

                withContext(Dispatchers.Main) {
                    _matrixConfig.value = null
                    _selectedDocuments.value = emptyList()
                    _extractedTextCache.value = ""

                    onSuccess(finalTopicId, textToStream, easy, mid, hard)
                }

            } catch (e: Exception) {
                Log.e("AiOcrTest", "❌ Lỗi tương tác CSDL: ${e.localizedMessage}")
            }
        }
    }

    fun clearState() {
        _matrixConfig.value = null
        _selectedDocuments.value = emptyList()
        _isLoading.value = false
        Log.d("AiOcrTest", "⚠️ clearState được gọi! _extractedTextCache cũ: '${_extractedTextCache.value}'")
        _extractedTextCache.value = ""
        Log.d("AiOcrTest", "🔄 Trạng thái luồng AI đã được đặt lại hoàn toàn sạch sẽ.")
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 1L
        } catch (e: Exception) {
            1L
        }
    }
}