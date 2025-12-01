package com.example.myapplication.ui.theme.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AlarmEntity
import com.example.myapplication.data.AlarmSettingData
import com.example.myapplication.data.AppDatabase
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
                            isLoading = false
                        )
                    }
                }
            } else {
                val now = LocalTime.now()
                _uiState.update {
                    it.copy(
                        hour = now.hour,
                        minute = now.minute,
                        isLoading = false
                    )
                }
            }
            updateTimeUntilAlarm()
        }
    }

    // --- Events (Logic xử lý) ---

    fun onTimeChanged(hour: Int, minute: Int) {
        _uiState.update { it.copy(hour = hour, minute = minute) }
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

    fun onDayToggle(dayCode: String) {
        _uiState.update { state ->
            val currentDays = state.daysOfWeek.toMutableSet()
            if (currentDays.contains(dayCode)) currentDays.remove(dayCode)
            else currentDays.add(dayCode)
            state.copy(daysOfWeek = currentDays)
        }
    }

    fun onSnoozeToggle(enabled: Boolean) {
        _uiState.update { it.copy(isSnoozeEnabled = enabled) }
    }

    fun onQuestionCountChanged(count: Int) {
        _uiState.update { it.copy(questionCount = count) }
    }

    fun saveAlarm() {
        viewModelScope.launch {
            val state = _uiState.value
            val alarmEntity = AlarmEntity(
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
                alarmDao.insertAlarm(alarmEntity)
            } else {
                alarmDao.updateAlarm(alarmEntity)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun onSnoozeDurationChanged(duration: Int) {
        _uiState.update { it.copy(snoozeDuration = duration) }
    }
}