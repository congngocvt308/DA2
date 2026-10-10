package com.cong.smartalarm.feature.qr

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cong.smartalarm.SmartAlarmApp
import com.cong.smartalarm.core.database.entity.AlarmQRLinkEntity
import com.cong.smartalarm.data.repository.QRCodeRepository
import com.cong.smartalarm.core.database.entity.QRCodeEntity
import com.cong.smartalarm.core.camera.BarcodeScannerProvider
import com.cong.smartalarm.core.media.BitmapDecodeUtils
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

class QRCodeViewModel(
    application: Application,
    private val qrCodeRepository: QRCodeRepository
) : AndroidViewModel(application) {
    
    // Danh sách tất cả QR codes đã lưu
    val allQRCodes: StateFlow<List<QRCodeEntity>> = qrCodeRepository.getAllQRCodes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    private val _uiState = MutableStateFlow(QRCodeUiState())
    val uiState: StateFlow<QRCodeUiState> = _uiState.asStateFlow()
    
    companion object {
        const val MAX_QR_CODES = 5
        const val MAX_QR_PER_ALARM = 3

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartAlarmApp
                QRCodeViewModel(app, app.container.qrCodeRepository)
            }
        }
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
                val currentCount = qrCodeRepository.getQRCodeCount()
                if (currentCount >= MAX_QR_CODES) {
                    onError("Bạn chỉ có thể lưu tối đa $MAX_QR_CODES mã. Vui lòng xóa bớt mã cũ.")
                    return@launch
                }
                
                // Kiểm tra trùng lặp
                val existing = qrCodeRepository.getQRCodeByValue(codeValue)
                if (existing != null) {
                    onError("Mã này đã được lưu với tên \"${existing.name}\"")
                    return@launch
                }
                
                val qrCode = QRCodeEntity(
                    name = name.trim(),
                    codeValue = codeValue,
                    codeType = codeType
                )
                
                qrCodeRepository.insertQRCode(qrCode)
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
                qrCodeRepository.deleteQRCode(qrCode)
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
                qrCodeRepository.updateQRCode(qrCode.copy(name = newName.trim()))
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
                val currentCount = qrCodeRepository.getQRLinkCountForAlarm(alarmId)
                if (currentCount >= MAX_QR_PER_ALARM) {
                    onError("Mỗi báo thức chỉ có thể sử dụng tối đa $MAX_QR_PER_ALARM mã")
                    return@launch
                }
                
                qrCodeRepository.insertAlarmQRLink(AlarmQRLinkEntity(alarmId, qrId))
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
            qrCodeRepository.deleteAlarmQRLink(alarmId, qrId)
        }
    }
    
    /**
     * Lấy danh sách QR codes cho một báo thức
     */
    fun getQRCodesForAlarm(alarmId: Int) = qrCodeRepository.getQRCodesForAlarm(alarmId)
    
    /**
     * Kiểm tra xem mã quét có hợp lệ để tắt báo thức không
     */
    suspend fun validateQRForAlarm(alarmId: Int, scannedCode: String): Boolean {
        return qrCodeRepository.isQRCodeValidForAlarm(alarmId, scannedCode)
    }
    
    /**
     * Quét mã từ ảnh (gallery)
     */
    fun scanFromImage(uri: Uri, onResult: (String, String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val context = getApplication<Application>()
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapDecodeUtils.decodeSampledBitmap(stream)
                    }
                }

                if (bitmap == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onError("Không thể đọc ảnh")
                    return@launch
                }

                val image = InputImage.fromBitmap(bitmap, 0)
                val scanner = BarcodeScannerProvider.scanner

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
                    .addOnCompleteListener { bitmap.recycle() }
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

