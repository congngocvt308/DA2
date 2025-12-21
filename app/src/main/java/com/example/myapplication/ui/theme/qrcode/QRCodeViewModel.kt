package com.example.myapplication.ui.theme.qrcode

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AlarmQRLinkEntity
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.QRCodeEntity
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class QRCodeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val scannedCode: String? = null,
    val scannedType: String? = null
)

class QRCodeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dao = AppDatabase.getDatabase(application).appDao()
    
    // Danh sách tất cả QR codes đã lưu
    val allQRCodes: StateFlow<List<QRCodeEntity>> = dao.getAllQRCodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _uiState = MutableStateFlow(QRCodeUiState())
    val uiState: StateFlow<QRCodeUiState> = _uiState.asStateFlow()
    
    companion object {
        const val MAX_QR_CODES = 5
        const val MAX_QR_PER_ALARM = 3
    }
    
    /**
     * Lưu QR/Barcode mới
     */
    fun saveQRCode(
        name: String,
        codeValue: String,
        codeType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Kiểm tra số lượng
                val currentCount = dao.getQRCodeCount()
                if (currentCount >= MAX_QR_CODES) {
                    onError("Bạn chỉ có thể lưu tối đa $MAX_QR_CODES mã. Vui lòng xóa bớt mã cũ.")
                    return@launch
                }
                
                // Kiểm tra trùng lặp
                val existing = dao.getQRCodeByValue(codeValue)
                if (existing != null) {
                    onError("Mã này đã được lưu với tên \"${existing.name}\"")
                    return@launch
                }
                
                val qrCode = QRCodeEntity(
                    name = name.trim(),
                    codeValue = codeValue,
                    codeType = codeType
                )
                
                dao.insertQRCode(qrCode)
                onSuccess()
            } catch (e: Exception) {
                onError("Lỗi khi lưu mã: ${e.message}")
            }
        }
    }
    
    /**
     * Xóa QR/Barcode
     */
    fun deleteQRCode(qrCode: QRCodeEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                dao.deleteQRCode(qrCode)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi khi xóa: ${e.message}")
            }
        }
    }
    
    /**
     * Cập nhật tên QR/Barcode
     */
    fun updateQRCodeName(qrCode: QRCodeEntity, newName: String) {
        viewModelScope.launch {
            try {
                dao.updateQRCode(qrCode.copy(name = newName.trim()))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Lỗi khi cập nhật: ${e.message}")
            }
        }
    }
    
    /**
     * Liên kết QR code với báo thức
     */
    fun linkQRToAlarm(alarmId: Int, qrId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentCount = dao.getQRLinkCountForAlarm(alarmId)
                if (currentCount >= MAX_QR_PER_ALARM) {
                    onError("Mỗi báo thức chỉ có thể sử dụng tối đa $MAX_QR_PER_ALARM mã")
                    return@launch
                }
                
                dao.insertAlarmQRLink(AlarmQRLinkEntity(alarmId, qrId))
                onSuccess()
            } catch (e: Exception) {
                onError("Lỗi khi liên kết: ${e.message}")
            }
        }
    }
    
    /**
     * Hủy liên kết QR code với báo thức
     */
    fun unlinkQRFromAlarm(alarmId: Int, qrId: Int) {
        viewModelScope.launch {
            dao.deleteAlarmQRLink(alarmId, qrId)
        }
    }
    
    /**
     * Lấy danh sách QR codes cho một báo thức
     */
    fun getQRCodesForAlarm(alarmId: Int) = dao.getQRCodesForAlarm(alarmId)
    
    /**
     * Kiểm tra xem mã quét có hợp lệ để tắt báo thức không
     */
    suspend fun validateQRForAlarm(alarmId: Int, scannedCode: String): Boolean {
        return dao.isQRCodeValidForAlarm(alarmId, scannedCode)
    }
    
    /**
     * Quét mã từ ảnh (gallery)
     */
    fun scanFromImage(uri: Uri, onResult: (String, String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeStream(inputStream)
                }
                inputStream?.close()
                
                if (bitmap == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Không thể đọc ảnh")
                    return@launch
                }
                
                val image = InputImage.fromBitmap(bitmap, 0)
                val scanner = BarcodeScanning.getClient()
                
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        
                        if (barcodes.isEmpty()) {
                            onError("Không tìm thấy mã QR/Barcode trong ảnh")
                        } else {
                            val barcode = barcodes.first()
                            val codeValue = barcode.rawValue ?: ""
                            val codeType = getCodeType(barcode.format)
                            
                            _uiState.value = _uiState.value.copy(
                                scannedCode = codeValue,
                                scannedType = codeType
                            )
                            onResult(codeValue, codeType)
                        }
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        onError("Lỗi khi quét ảnh: ${e.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                onError("Lỗi khi quét ảnh: ${e.message}")
            }
        }
    }
    
    /**
     * Xử lý kết quả quét từ camera
     */
    fun processScannedBarcode(barcode: Barcode): Pair<String, String>? {
        val codeValue = barcode.rawValue ?: return null
        val codeType = getCodeType(barcode.format)
        
        _uiState.value = _uiState.value.copy(
            scannedCode = codeValue,
            scannedType = codeType
        )
        
        return Pair(codeValue, codeType)
    }
    
    private fun getCodeType(format: Int): String {
        return when (format) {
            Barcode.FORMAT_QR_CODE -> "QR"
            Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93, Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_ITF -> "BARCODE"
            else -> "OTHER"
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
    
    fun clearScannedCode() {
        _uiState.value = _uiState.value.copy(
            scannedCode = null,
            scannedType = null
        )
    }
}

