package com.example.myapplication.ui.theme.alarm

import android.app.Application
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.alarm_logic.AlarmScheduler
import com.example.myapplication.data.AlarmEntity
import com.example.myapplication.data.AlarmFaceMissionConfigEntity
import com.example.myapplication.data.AlarmQRLinkEntity
import com.example.myapplication.data.AlarmSelectedQuestionEntity
import com.example.myapplication.data.AlarmSettingData
import com.example.myapplication.data.AlarmTopicLink
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.MissionQuestion
import com.example.myapplication.data.MissionTopic
import com.example.myapplication.ui.theme.mission.MissionViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.collections.filter

class AlarmSettingsViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val alarmDao = AppDatabase.getDatabase(application).appDao()
    private val alarmId: Int = savedStateHandle.get<Int>("alarmId") ?: -1
    private val _uiState = MutableStateFlow(AlarmSettingData())
    val uiState: StateFlow<AlarmSettingData> = _uiState.asStateFlow()

    init {
        loadAlarm()
    }

    private fun loadAlarm() {
        viewModelScope.launch {
            if (alarmId != -1) {
                val alarm = alarmDao.getAlarmById(alarmId)
                if (alarm != null) {
                    // 1. Load các câu hỏi lẻ (Manual Selection)
                    val selectedQuestionEntities = alarmDao.getSelectedQuestionsForAlarmOnce(alarmId)
                    val selectedQuestions = selectedQuestionEntities.map { entity ->
                        if (entity.questionId < 0) {
                            // Câu hỏi mặc định
                            val defaultId = kotlin.math.abs(entity.questionId)
                            MissionQuestion(
                                id = entity.questionId,
                                text = getDefaultQuestionText(defaultId),
                                isSelected = true
                            )
                        } else {
                            // Câu hỏi từ database
                            val question = alarmDao.getQuestionById(entity.questionId)
                            MissionQuestion(
                                id = entity.questionId,
                                text = question?.prompt ?: "Câu hỏi đã bị xóa",
                                isSelected = true
                            )
                        }
                    }

                    // 2. Load các Topic đã chọn Full (Quan trọng: Phải load cả câu hỏi bên trong)
                    val selectedTopicLinks = alarmDao.getTopicLinksForAlarmOnce(alarmId)

                    val restoredTopics = selectedTopicLinks.map { link ->
                        // Lấy tên Topic
                        val topicName = alarmDao.getTopicNameById(link.topicId) ?: ""

                        // 🚨 QUAN TRỌNG: Lấy danh sách câu hỏi của Topic này từ DB
                        // Dùng .first() để lấy giá trị hiện tại từ Flow mà AppDao trả về
                        val questionsEntities = alarmDao.getQuestionsByTopic(link.topicId).first()

                        val topicQuestions = questionsEntities.map { q ->
                            MissionQuestion(id = q.questionId, text = q.prompt, isSelected = true)
                        }

                        MissionTopic(
                            id = link.topicId,
                            name = topicName,
                            questions = topicQuestions, // Phải có list này thì logic .any trong saveMissionData mới chạy được
                            isSelected = true, // Đánh dấu là chọn tất cả
                            isExpanded = false
                        )
                    }

                    // 3. Load selected QR codes
                    val selectedQRCodes = alarmDao.getQRCodesForAlarmOnce(alarmId)
                    val selectedQRCodeIds = selectedQRCodes.map { it.qrId }

                    // 4. Load Face Mission Config
                    val faceConfig = alarmDao.getFaceMissionConfigByAlarmId(alarmId)

                    _uiState.update {
                        it.copy(
                            id = alarm.alarmId,
                            hour = alarm.hour,
                            minute = alarm.minute,
                            label = alarm.label ?: "",
                            daysOfWeek = alarm.daysOfWeek,
                            isSnoozeEnabled = alarm.snoozeEnabled,
                            snoozeDuration = alarm.snoozeDuration,
                            ringtoneUri = alarm.ringtoneUri ?: "",
                            questionCount = alarm.questionCount,
                            selectedQuestions = selectedQuestions,
                            selectedQRCodeIds = selectedQRCodeIds,
                            faceMissionConfig = faceConfig,

                            // SỬA Ở ĐÂY: Gán List<MissionTopic> thay vì Set ID
                            selectedTopicIds = restoredTopics,

                            isLoading = false
                        )
                    }
                }
            } else {
                val now = LocalTime.now()
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
                _uiState.update {
                    it.copy(
                        hour = now.hour,
                        minute = now.minute,
                        ringtoneUri = defaultUri,
                        volume = 0.7f,
                        isLoading = false
                    )
                }
            }
            updateTimeUntilAlarm()
        }
    }
    
    private fun getDefaultQuestionText(id: Int): String {
        return when (id) {
            1 -> "Tác phẩm nào KHÔNG thuộc Tứ đại danh tác?"
            2 -> "1 + 1 = ?"
            3 -> "Thủ đô Việt Nam?"
            4 -> "2 x 2 = ?"
            5 -> "Loại hình MVVM?"
            else -> ""
        }
    }

    fun setupQuickAlarm(minutesToAdd: Int) {
        val now = LocalDateTime.now().plusMinutes(minutesToAdd.toLong())
        _uiState.update {
            it.copy(
                id = -1, // Đảm bảo là tạo mới
                hour = now.hour,
                minute = now.minute,
                label = "Báo thức nhanh",
                daysOfWeek = emptySet(), // Không lặp lại
                isSnoozeEnabled = true,
                isLoading = false
            )
        }
        updateTimeUntilAlarm()
    }

    fun onLabelChanged(newLabel: String) {
        _uiState.update { it.copy(label = newLabel) }
    }

    fun updateHour(newHour: Int) {
        _uiState.update { it.copy(hour = newHour) }
        updateTimeUntilAlarm()
    }
    fun updateMinute(newMinute: Int) {
        _uiState.update { it.copy(minute = newMinute) }
        updateTimeUntilAlarm()
    }
    private val allDays = setOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    fun toggleRepeatDaily(isChecked: Boolean) {
        _uiState.update { state ->
            state.copy(
                daysOfWeek = if (isChecked) allDays else emptySet()
            )
        }
    }
    fun toggleDay(dayCode: String) {
        _uiState.update { state ->
            val currentDays = state.daysOfWeek.toMutableSet()
            if (currentDays.contains(dayCode)) {
                currentDays.remove(dayCode)
            } else {
                currentDays.add(dayCode)
            }
            state.copy(daysOfWeek = currentDays)
        }
    }

    fun updateVolume(newVolume: Float) {
        _uiState.update { it.copy(volume = newVolume) }
    }

    fun updateRingtone(uri: String) {
        _uiState.update { it.copy(ringtoneUri = uri) }
    }

    private fun updateTimeUntilAlarm() {
        val state = _uiState.value
        val now = LocalDateTime.now()
        val nextAlarmTime = findNextAlarmTime(
            now = now,
            targetHour = state.hour,
            targetMinute = state.minute,
            daysOfWeek = state.daysOfWeek
        )
        val duration = Duration.between(now, nextAlarmTime)
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val message = when {
            days < 1 -> {
                if (hours == 0L && minutes == 0L) "Đổ chuông trong vòng chưa đầy 1 phút."
                else "Đổ chuông sau $hours giờ $minutes phút."
            }
            else -> "Đổ chuông sau $days ngày."
        }

        _uiState.update { it.copy(timeUntilAlarm = message) }
    }

    private fun findNextAlarmTime(
        now: LocalDateTime,
        targetHour: Int,
        targetMinute: Int,
        daysOfWeek: Set<String>
    ): LocalDateTime {
        var target = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
        if (daysOfWeek.isEmpty()) {
            if (target.isBefore(now) || target.isEqual(now)) {
                target = target.plusDays(1)
            }
            return target
        }
        for (i in 0..7) {
            val dayCode = getDayCode(target.dayOfWeek)
            if (daysOfWeek.contains(dayCode) && target.isAfter(now)) {
                return target
            }
            target = target.plusDays(1)
        }
        return target
    }

    private fun getDayCode(dayOfWeek: java.time.DayOfWeek): String {
        return when (dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "T2"
            java.time.DayOfWeek.TUESDAY -> "T3"
            java.time.DayOfWeek.WEDNESDAY -> "T4"
            java.time.DayOfWeek.THURSDAY -> "T5"
            java.time.DayOfWeek.FRIDAY -> "T6"
            java.time.DayOfWeek.SATURDAY -> "T7"
            java.time.DayOfWeek.SUNDAY -> "CN"
        }
    }

    fun onSnoozeToggle(enabled: Boolean) {
        _uiState.update { it.copy(isSnoozeEnabled = enabled) }
    }


    fun saveAlarm() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val scheduler = AlarmScheduler(context)
            val state = _uiState.value
            var alarmEntity = AlarmEntity(
                alarmId = if (state.id == -1) 0 else state.id,
                hour = state.hour,
                minute = state.minute,
                label = state.label,
                daysOfWeek = state.daysOfWeek,
                isEnabled = true,
                questionCount = state.questionCount,
                snoozeEnabled = state.isSnoozeEnabled,
                snoozeDuration = state.snoozeDuration,
                ringtoneUri = state.ringtoneUri
            )
            val finalAlarmId = if (state.id == -1) {
                val newId = alarmDao.insertAlarm(alarmEntity).toInt()
                alarmEntity = alarmEntity.copy(alarmId = newId)
                newId
            } else {
                alarmDao.updateAlarm(alarmEntity)
                state.id
            }
            
            // 🚨 LƯU CÁC CÂU HỎI ĐƯỢC CHỌN VÀO DATABASE
            saveMissionData(finalAlarmId, state.selectedQuestions, state.selectedTopicIds)

            // 4. Lưu QR Code và đặt lịch báo thức
            saveSelectedQRCodes(finalAlarmId, state.selectedQRCodeIds)

            // 5. Lưu Face Mission Config
            state.faceMissionConfig?.let {
                alarmDao.insertOrUpdateFaceMissionConfig(it.copy(alarmId = finalAlarmId))
            }

            scheduler.schedule(alarmEntity)

            _uiState.update { it.copy(isSaved = true, id = finalAlarmId) }
        }
    }
    
    private suspend fun saveSelectedQuestions(alarmId: Int, questions: List<MissionQuestion>) {
        // Xóa các câu hỏi cũ của báo thức này
        alarmDao.clearSelectedQuestionsForAlarm(alarmId)
        
        // Lưu các câu hỏi mới được chọn
        questions.forEach { question ->
            // Xử lý cả câu hỏi mặc định (id bắt đầu bằng "default_") và câu hỏi từ database
            val entity = com.example.myapplication.data.AlarmSelectedQuestionEntity(
                alarmId = alarmId,
                questionId = question.id, // Dùng trực tiếp question.id kiểu Int
                topicId = null
            )
            alarmDao.insertSelectedQuestion(entity)
        }
    }

    fun onSnoozeDurationChanged(duration: Int) {
        _uiState.update { it.copy(snoozeDuration = duration) }
    }

    fun updateMission(count: Int, questions: List<MissionQuestion>, topics: List<MissionTopic>) {
        _uiState.update { currentState ->
            currentState.copy(
                questionCount = count,
                selectedQuestions = questions,
                selectedTopicIds = topics
            )
        }
    }
    
    fun updateSelectedQRCodes(qrCodeIds: List<Int>) {
        _uiState.update { currentState ->
            currentState.copy(selectedQRCodeIds = qrCodeIds)
        }
    }

    fun updateFaceMissionConfig(config: AlarmFaceMissionConfigEntity) {
        _uiState.update { currentState ->
            currentState.copy(faceMissionConfig = config)
        }
    }
    
    private suspend fun saveSelectedQRCodes(alarmId: Int, qrCodeIds: List<Int>) {
        // Xóa các liên kết cũ
        alarmDao.clearQRLinksForAlarm(alarmId)
        
        // Lưu các liên kết mới
        qrCodeIds.forEach { qrId ->
            alarmDao.insertAlarmQRLink(AlarmQRLinkEntity(alarmId = alarmId, qrId = qrId))
        }
    }

    // Trong AlarmSettingsViewModel.kt

    private suspend fun saveMissionData(
        alarmId: Int,
        questions: List<MissionQuestion>,
        topics: List<MissionTopic>
    ) {
        alarmDao.clearSelectedQuestionsForAlarm(alarmId)
        alarmDao.clearAlarmTopicLinks(alarmId)

        val fullSelectedTopicIds = topics.filter { it.isSelected }
            .map { it.id }
            .toSet()

        fullSelectedTopicIds.forEach { topicId ->
            if (topicId != MissionViewModel.DEFAULT_TOPIC_ID) {
                alarmDao.insertAlarmTopicLink(
                    AlarmTopicLink(alarmId = alarmId, topicId = topicId)
                )
            }
        }

        // Lưu các câu hỏi lẻ (đặc biệt là câu mặc định ID âm)
        questions.forEach { question ->
            val isDefault = question.id < 0

            // Tìm xem câu hỏi này thuộc Topic nào
            val parentTopic = topics.find { topic ->
                topic.questions.any { it.id == question.id }
            }

            if (isDefault) {
                // Câu hỏi mặc định luôn lưu vào bảng SelectedQuestion
                alarmDao.insertSelectedQuestion(
                    AlarmSelectedQuestionEntity(alarmId = alarmId, questionId = question.id)
                )
            } else if (parentTopic != null && !fullSelectedTopicIds.contains(parentTopic.id)) {
                // CHỈ LƯU vào bảng này nếu Topic cha của nó KHÔNG được chọn toàn bộ
                // (Tức là người dùng chỉ chọn vài câu lẻ trong Topic đó)
                alarmDao.insertSelectedQuestion(
                    AlarmSelectedQuestionEntity(alarmId = alarmId, questionId = question.id)
                )
            }
        }
    }
}