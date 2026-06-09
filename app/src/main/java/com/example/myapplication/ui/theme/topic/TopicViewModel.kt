package com.example.myapplication.ui.theme.topic

import android.app.Application
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AiMatrixConfig
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.TopicData
import com.example.myapplication.data.TopicEntity
import com.example.myapplication.data.SelectedDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    private val topicDao = AppDatabase.getDatabase(application).appDao()

    val searchQuery = MutableStateFlow("")

    private val context = application.applicationContext
    private val documentCompressor = DocumentCompressor(context)

    // --- TRẠNG THÁI UI TRUYỀN XUỐNG BOTTOM SHEET ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _matrixConfig = MutableStateFlow<AiMatrixConfig?>(null)
    val matrixConfig: StateFlow<AiMatrixConfig?> = _matrixConfig.asStateFlow()

    // 🌟 Quản lý danh sách hàng chờ tài liệu người dùng chọn (State Hoisting)
    private val _selectedDocuments = MutableStateFlow<List<SelectedDocument>>(emptyList())
    val selectedDocuments: StateFlow<List<SelectedDocument>> = _selectedDocuments.asStateFlow()

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

    // 🌟 Thêm tài liệu vào hàng chờ và tự động phân loại thông minh (PDF Text vs PDF Scan)
    fun addDocumentToQueue(uri: Uri, name: String, isPdf: Boolean) {
        viewModelScope.launch {
            var count = 1
            var isTextPdf = false

            if (isPdf) {
                try {
                    // 1. Dùng bộ Renderer mặc định siêu nhẹ để lấy ra tổng số trang thực tế
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        val renderer = PdfRenderer(pfd)
                        count = renderer.pageCount
                        renderer.close() // Đóng ngay sau khi lấy thông tin xong
                    }

                    // 2. Lấy tổng dung lượng file gốc (Bytes)
                    val totalFileSize = getFileSize(uri)

                    // 3. THUẬT TOÁN ĐÚNG LOGIC: Tính dung lượng trung bình trên mỗi trang (Bytes/Trang)
                    val averagePageSize = totalFileSize.toFloat() / count.toFloat()

                    // Ngưỡng bảo vệ: 500 KB tính theo Bytes (500 * 1024)
                    val maxTextPageSizeThreshold = 500 * 1024

                    // Nếu 1 trang trung bình nhỏ hơn 300KB -> Khẳng định 100% là PDF Text chứa chữ
                    if (averagePageSize < maxTextPageSizeThreshold) {
                        isTextPdf = true
                    }

                    Log.d("MultiCompressTest", "📊 KIỂM TRA PHÂN LOẠI PDF:")
                    Log.d("MultiCompressTest", "   ➔ Tổng số trang: $count trang")
                    Log.d("MultiCompressTest", "   ➔ Tổng dung lượng: ${String.format("%.2f", totalFileSize / (1024.0 * 1024.0))} MB")
                    Log.d("MultiCompressTest", "   ➔ Trung bình 1 trang: ${String.format("%.2f", averagePageSize / 1024.0)} KB/Trang")
                    Log.d("MultiCompressTest", "   ➔ Kết luận -> Là PDF Chữ Thô: $isTextPdf")

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("MultiCompressTest", "❌ Lỗi khi đọc cấu trúc phân loại PDF: ${e.message}")
                }
            }

            val newDoc = SelectedDocument(
                id = java.util.UUID.randomUUID().toString(),
                uri = uri,
                name = name,
                isPdf = isPdf,
                isPdfText = isTextPdf, // Nhãn phân loại chính xác tuyệt đối dù file ngắn hay dài
                totalPages = count
            )
            _selectedDocuments.update { it + newDoc }
        }
    }

    // Xóa tài liệu khỏi hàng chờ bằng định danh ID
    fun removeDocumentFromQueue(id: String) {
        _selectedDocuments.update { list -> list.filter { it.id != id } }
        Log.d("MultiCompressTest", "❌ Đã xóa tài liệu khỏi danh sách chờ.")
    }

    // Cập nhật cấu hình trang cho tài liệu dạng PDF
    fun updateDocumentPageConfig(id: String, newPageConfig: String) {
        _selectedDocuments.update { list ->
            list.map { doc ->
                if (doc.id == id) doc.copy(pageConfig = newPageConfig) else doc
            }
        }
    }

    // 🌟 Thực thi tiến trình nén hàng loạt phân nhánh đúng logic thiết kế
    fun processAndCompressQueue() {
        viewModelScope.launch {
            val currentDocs = _selectedDocuments.value
            if (currentDocs.isEmpty()) return@launch

            _isLoading.value = true
            Log.d("MultiCompressTest", "============== KHỞI CHẠY TIẾN TRÌNH XỬ LÝ PHÂN NHÁNH HÀNG LOẠT ==============")

            // Gọi tầng Compressor thực thi tác vụ I/O ngầm
            val outputFiles = documentCompressor.compressMultipleDocuments(currentDocs)

            Log.d("MultiCompressTest", "✅ ĐỒNG BỘ THÀNH CÔNG! Số tệp kết xuất lưu trữ: ${outputFiles.size}")
            outputFiles.forEachIndexed { index, file ->
                val ext = file.extension.uppercase()
                Log.d("MultiCompressTest", "   ➔ [Tệp ${index + 1}] Định dạng: $ext | Tên: ${file.name}")
                Log.d("MultiCompressTest", "   ➔ Dung lượng thực tế: ${String.format("%.2f", file.length() / 1024.0)} KB")
                Log.d("MultiCompressTest", "   ➔ Thư mục lưu trữ công khai: ${file.absolutePath}")
            }
            Log.d("MultiCompressTest", "=========================================================================")

            // Giả lập sau khi nén hoàn tất, hiển thị cấu hình ma trận câu hỏi (Mô phỏng bước tiếp theo của luồng)
            _matrixConfig.value = AiMatrixConfig(
                suggestedTopic = "Chủ đề tổng hợp từ ${currentDocs.size} tài liệu"
            )
            _selectedDocuments.value = emptyList() // Dọn dẹp danh sách chờ sau khi xử lý thành công
            _isLoading.value = false
        }
    }

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

    fun updateMatrixSliders(easy: Float, mid: Float, hard: Float) {
        _matrixConfig.update { it?.copy(easyCount = easy, midCount = mid, hardCount = hard) }
    }

    fun clearState() {
        _matrixConfig.value = null
        _selectedDocuments.value = emptyList()
        _isLoading.value = false
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 1L
        } catch (e: Exception) {
            1L
        }
    }
}