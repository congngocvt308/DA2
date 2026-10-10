package com.cong.smartalarm.feature.smile

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cong.smartalarm.feature.smile.FaceMissionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale
import android.graphics.PointF
import android.media.AudioManager
import android.os.Bundle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cong.smartalarm.SmartAlarmApp
import com.cong.smartalarm.data.repository.AlarmRepository
import com.cong.smartalarm.data.repository.PictureRepository
import com.cong.smartalarm.feature.smile.FaceMissionConfig
import com.cong.smartalarm.core.database.entity.PictureEntity
import com.cong.smartalarm.core.media.SoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FaceMissionViewModel(
    context: Context,
    private val alarmId: Int = -1,
    private val alarmRepository: AlarmRepository,
    private val pictureRepository: PictureRepository,
    initialBgUri: String? = null
) : ViewModel(), TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow(FaceMissionUiState(backgroundImageUri = initialBgUri))
    val uiState: StateFlow<FaceMissionUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var lastTtsTime = 0L

    private var currentScore = 0f
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val soundPlayer = SoundPlayer(context)
    private var targetScore = FaceMissionConfig.TARGET_SCORE

    init {
        loadAlarmConfig()
    }

    private fun loadAlarmConfig() {
        viewModelScope.launch {
            if (alarmId != -1) {
                val config = alarmRepository.getFaceMissionConfigByAlarmId(alarmId)
                config?.let { c ->
                    targetScore = (c.requiredHoldDurationSec * 20f).coerceAtLeast(20f)
                    _uiState.update { it.copy(
                        isConfigLoaded = true,
                        backgroundImageUri = c.backgroundImageUri,
                        requiredHoldDurationSec = c.requiredHoldDurationSec,
                        eyeOpenSoundUri = c.eyeOpenSoundUri,
                        smileSoundUri = c.smileSoundUri,
                        comboSoundUri = c.comboSoundUri,
                        holdPoseSoundUri = c.holdPoseSoundUri
                    ) }
                } ?: run {
                    // Nếu không tìm thấy config trong DB, vẫn đánh dấu là đã load xong (dùng mặc định)
                    _uiState.update { it.copy(isConfigLoaded = true) }
                }
            } else {
                // Trường hợp test không có alarmId
                _uiState.update { it.copy(isConfigLoaded = true) }
            }
        }
    }

    fun savePictureToDb(imageUri: String, description: String, alarmHistoryId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val picture = PictureEntity(
                userId = 1,
                alarmHistoryId = alarmHistoryId,
                imageUri = imageUri,
                description = description,
                createdAt = System.currentTimeMillis()
            )
            pictureRepository.insertPicture(picture)
        }
    }

    fun updateFaceTracking(
        isQualified: Boolean,
        leftEyeProb: Float,
        rightEyeProb: Float,
        smileProb: Float,
        leftEye: PointF?,
        rightEye: PointF?,
        mouth: PointF?,
        faceWidthPx: Float,
        headRotationZ: Float,
        cWidth: Int,
        cHeight: Int
    ) {
        if (_uiState.value.isSuccess) return

        val leftEyeValid = leftEyeProb >= FaceMissionConfig.THRESHOLD_EAR
        val rightEyeValid = rightEyeProb >= FaceMissionConfig.THRESHOLD_EAR
        val smileValid = smileProb >= FaceMissionConfig.THRESHOLD_SMILE
        val isBothEyesOpen = leftEyeValid && rightEyeValid

        val (isWarning, msg) = when {
            !isBothEyesOpen && !smileValid -> true to "Mở mắt và cười lên nào! ☀️"
            !isBothEyesOpen -> true to "Mở mắt ra nào! 👀"
            !smileValid -> true to "Cười tươi lên nào! 😄"
            else -> false to "Giữ nguyên nhé! 🎯"
        }

        if (isQualified) {
            currentScore += FaceMissionConfig.SCORE_INCREMENT
            if (currentScore >= targetScore) {
                currentScore = targetScore
                _uiState.update { it.copy(progress = 1f, isWarning = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        progress = currentScore / targetScore,
                        isWarning = false,
                        warningMessage = msg,
                        leftEyePoint = leftEye, rightEyePoint = rightEye, mouthPoint = mouth,
                        isLeftEyeOpen = leftEyeValid,   // Độc lập mắt trái
                        isRightEyeOpen = rightEyeValid,
                        isSmileValid = smileValid,
                        headRotationZ = headRotationZ,
                        cameraWidth = cWidth, cameraHeight = cHeight
                    )
                }
            }
        } else {
            currentScore -= FaceMissionConfig.SCORE_DECREMENT
            if (currentScore < 0f) currentScore = 0f

            _uiState.update {
                it.copy(
                    progress = currentScore / targetScore,
                    isWarning = isWarning,
                    warningMessage = msg,
                    leftEyePoint = leftEye, rightEyePoint = rightEye, mouthPoint = mouth,
                    isLeftEyeOpen = leftEyeValid,   // Độc lập mắt trái
                    isRightEyeOpen = rightEyeValid, // Độc lập mắt phải
                    isSmileValid = smileValid,
                    faceWidthPx = if (faceWidthPx > 0f) faceWidthPx else it.faceWidthPx,
                    headRotationZ = headRotationZ,
                    cameraWidth = cWidth, cameraHeight = cHeight
                )
            }
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTtsTime > 2500L) {
            lastTtsTime = currentTime
            
            val config = _uiState.value
            val customSoundUri = when {
                !isBothEyesOpen && !smileValid -> config.comboSoundUri
                !isBothEyesOpen -> config.eyeOpenSoundUri
                !smileValid -> config.smileSoundUri
                else -> config.holdPoseSoundUri
            }

            if (!customSoundUri.isNullOrEmpty()) {
                soundPlayer.playOrUpdateVolume(customSoundUri, 1.0f)
            } else {
                // 🎯 Đẩy âm lượng kênh ALARM hoặc MUSIC của điện thoại lên mức MAX (100%)
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    maxVolume,
                    0 // Không hiện UI thanh âm lượng hệ thống
                )

                // 🎯 Tạo Bundle cấu hình phát max volume cho TTS
                val params = Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f) // 1.0f = 100% volume
                    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM) // Phát qua kênh Báo thức
                }

                tts.speak(
                    msg,
                    TextToSpeech.QUEUE_FLUSH,
                    params,
                    "FaceMissionTTS_${System.currentTimeMillis()}"
                )
            }
        }
    }

    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) tts.language = Locale("vi", "VN") }
    override fun onCleared() { tts.stop(); tts.shutdown(); super.onCleared() }

    companion object {
        // TextToSpeech cần Context -> dùng Application để không giữ Activity
        fun factory(alarmId: Int, initialBgUri: String? = null): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartAlarmApp
                FaceMissionViewModel(
                    context = app,
                    alarmId = alarmId,
                    alarmRepository = app.container.alarmRepository,
                    pictureRepository = app.container.pictureRepository,
                    initialBgUri = initialBgUri
                )
            }
        }
    }
}
