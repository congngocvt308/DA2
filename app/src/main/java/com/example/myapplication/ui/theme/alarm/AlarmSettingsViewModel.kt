package com.example.myapplication.ui.theme.alarm

import android.app.Application
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.alarm_logic.AlarmScheduler
import com.example.myapplication.data.AlarmEntity
import com.example.myapplication.data.AlarmSettingData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.MissionQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

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
                    // Load các câu hỏi đã chọn
                    val selectedQuestionEntities = alarmDao.getSelectedQuestionsForAlarmOnce(alarmId)
                    val selectedQuestions = selectedQuestionEntities.map { entity ->
                        if (entity.questionId < 0) {
                            // Câu hỏi mặc định
                            val defaultId = -entity.questionId
                            MissionQuestion(
                                id = "default_$defaultId",
                                text = getDefaultQuestionText(defaultId),
                                isSelected = true
                            )
                        } else {
                            // Câu hỏi từ database
                            val question = alarmDao.getQuestionById(entity.questionId)
                            MissionQuestion(
                                id = entity.questionId.toString(),
                                text = question?.prompt ?: "",
                                isSelected = true
                            )
                        }
                    }
                    
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
            if (state.id == -1) {
                val newId = alarmDao.insertAlarm(alarmEntity)

                // 🚨 QUAN TRỌNG: Cập nhật lại ID mới vào entity để scheduler dùng đúng ID này làm RequestCode
                alarmEntity = alarmEntity.copy(alarmId = newId.toInt())

                // 3. Truyền alarmEntity (đã có
                scheduler.schedule(alarmEntity)
            } else {
                // 4. Cập nhật báo thức cũ
                alarmDao.updateAlarm(alarmEntity)

                // Truyền alarmEntity vào scheduler
                scheduler.schedule(alarmEntity)
            }
            
            // 🚨 LƯU CÁC CÂU HỎI ĐƯỢC CHỌN VÀO DATABASE
            saveSelectedQuestions(alarmEntity.alarmId, state.selectedQuestions)
            
            _uiState.update { it.copy(isSaved = true, id = alarmEntity.alarmId) }
        }
    }
    
    private suspend fun saveSelectedQuestions(alarmId: Int, questions: List<MissionQuestion>) {
        // Xóa các câu hỏi cũ của báo thức này
        alarmDao.clearSelectedQuestionsForAlarm(alarmId)
        
        // Lưu các câu hỏi mới được chọn
        questions.forEach { question ->
            // Xử lý cả câu hỏi mặc định (id bắt đầu bằng "default_") và câu hỏi từ database
            val questionId = if (question.id.startsWith("default_")) {
                // Câu hỏi mặc định: chuyển "default_1" -> -1, "default_2" -> -2, ...
                val defaultIndex = question.id.removePrefix("default_").toIntOrNull() ?: return@forEach
                -defaultIndex
            } else {
                question.id.toIntOrNull() ?: return@forEach
            }
            
            val entity = com.example.myapplication.data.AlarmSelectedQuestionEntity(
                alarmId = alarmId,
                questionId = questionId,
                topicId = null
            )
            alarmDao.insertSelectedQuestion(entity)
        }
    }

    fun onSnoozeDurationChanged(duration: Int) {
        _uiState.update { it.copy(snoozeDuration = duration) }
    }

    fun updateMission(count: Int, questions: List<MissionQuestion>) {
        _uiState.update { currentState ->
            currentState.copy(
                questionCount = count,
                selectedQuestions = questions
            )
        }
    }
}