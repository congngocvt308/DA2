package com.example.myapplication.ui.theme.facedetection

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.FaceMissionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import android.graphics.PointF
import com.example.myapplication.data.FaceMissionConfig


class FaceMissionViewModel(context: Context) : ViewModel(), TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow(FaceMissionUiState())
    val uiState: StateFlow<FaceMissionUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var lastTtsTime = 0L

    private var currentScore = 0f

    fun updateFaceTracking(
        isQualified: Boolean, ear: Float, smileProb: Float,
        leftEye: PointF?, rightEye: PointF?, mouth: PointF?,
        cWidth: Int, cHeight: Int
    ) {
        if (_uiState.value.isSuccess) return

        val eyeValid = ear >= FaceMissionConfig.THRESHOLD_EAR
        val smileValid = smileProb >= FaceMissionConfig.THRESHOLD_SMILE

        if (isQualified) {
            currentScore += FaceMissionConfig.SCORE_INCREMENT
            if (currentScore >= FaceMissionConfig.TARGET_SCORE) {
                currentScore = FaceMissionConfig.TARGET_SCORE
                _uiState.update { it.copy(progress = 1f, isWarning = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        progress = currentScore / FaceMissionConfig.TARGET_SCORE, isWarning = false,
                        leftEyePoint = leftEye, rightEyePoint = rightEye, mouthPoint = mouth,
                        isEyeOpenValid = eyeValid, isSmileValid = smileValid,
                        cameraWidth = cWidth, cameraHeight = cHeight
                    )
                }
            }
        } else {
            currentScore -= FaceMissionConfig.SCORE_DECREMENT
            if (currentScore < 0f) currentScore = 0f

            val msg = when {
                !eyeValid && !smileValid -> "Mở mắt ra và Cười tươi lên! 😡"
                !eyeValid -> "Mở mắt ra nào, đừng ngủ nướng! 👁️"
                else -> "Cười tươi lên xem nào! ☕"
            }

            _uiState.update {
                it.copy(
                    progress = currentScore / FaceMissionConfig.TARGET_SCORE, isWarning = true, warningMessage = msg,
                    leftEyePoint = leftEye, rightEyePoint = rightEye, mouthPoint = mouth,
                    isEyeOpenValid = eyeValid, isSmileValid = smileValid,
                    cameraWidth = cWidth, cameraHeight = cHeight
                )
            }

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTtsTime > 2500L) {
                tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null)
                lastTtsTime = currentTime
            }
        }
    }

    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale("vi", "VN") }
    override fun onCleared() { tts.stop(); tts.shutdown(); super.onCleared() }
}

class FaceMissionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FaceMissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FaceMissionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}