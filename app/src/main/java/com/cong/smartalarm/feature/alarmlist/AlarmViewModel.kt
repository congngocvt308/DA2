package com.cong.smartalarm.feature.alarmlist

import com.cong.smartalarm.core.alarm.NextAlarmTime
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.cong.smartalarm.core.alarm.AlarmScheduler
import com.cong.smartalarm.feature.alarmlist.AlarmData
import com.cong.smartalarm.core.database.entity.AlarmEntity
import com.cong.smartalarm.feature.alarmedit.AlarmSettingData
import com.cong.smartalarm.SmartAlarmApp
import com.cong.smartalarm.data.repository.AlarmRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration
import java.util.Calendar

enum class SortType{ DEFAULT, ACTIVE_FIRST}

class AlarmViewModel(
    application: Application,
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : AndroidViewModel(application) {

    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    private val tickerFlow = flow {
        while (true) {
            emit(LocalDateTime.now()) // Bắn ra thời gian hiện tại
            delay(60000) // Đợi 60 giây rồi lặp lại
        }
    }

    val timeUntilNextAlarms: StateFlow<String> = combine(
        alarmRepository.getAllAlarms(), // Luồng 1: Dữ liệu DB
        tickerFlow               // Luồng 2: Thời gian trôi
    ) { entities, now ->         // 'now' được lấy từ tickerFlow

        // Lọc các báo thức đang bật
        val activeAlarms = entities.filter { it.isEnabled }

        if (activeAlarms.isEmpty()) {
            "Không có báo thức sắp tới"
        } else {
            // Tính thời gian reo của TẤT CẢ báo thức đang bật
            val nextRingTimes = activeAlarms.map { alarm ->
                NextAlarmTime.after(
                    now,
                    alarm.hour,
                    alarm.minute,
                    alarm.daysOfWeek
                )
            }

            // Tìm cái gần nhất (nhỏ nhất)
            val nearestTime = nextRingTimes.minOrNull()

            if (nearestTime != null) {
                // Tính khoảng cách từ 'now' đến 'nearestTime'
                val duration = Duration.between(now, nearestTime)
                formatDuration(duration)
            } else {
                "Không có báo thức sắp tới"
            }
        }
    }
        .distinctUntilChanged()
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Đang tính toán..."
    )

    private fun formatDuration(duration: Duration): String {
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60

        return when {
            days < 1 -> {
                if (hours == 0L && minutes == 0L) "Báo thức sau chưa đầy 1 phút"
                else "Báo thức sau $hours giờ $minutes phút"
            }
            days == 1L -> "Báo thức sau 1 ngày"
            else -> "Báo thức sau $days ngày"
        }
    }

    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    val alarms: StateFlow<List<AlarmData>> = combine(
        alarmRepository.getAllAlarms(),
        _sortType
    ){ entities, sortType ->
        val dataList = entities.map{ it.toAlarmData()}
        when(sortType){
            SortType.DEFAULT -> { dataList.sortedBy { it.id }}
            SortType.ACTIVE_FIRST -> {
                dataList.sortedWith(compareByDescending<AlarmData>{it.isEnabled}.thenBy { it.id })
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleAlarm(id: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(id) ?: return@launch
            val updatedAlarm = alarm.copy(isEnabled = isEnabled)
            alarmRepository.updateAlarm(updatedAlarm)

            if (isEnabled) {
                alarmScheduler.schedule(updatedAlarm)
            } else {
                alarmScheduler.cancel(updatedAlarm)
            }
        }
    }

    fun AlarmEntity.toAlarmData(): AlarmData {
        val timeString = String.format("%02d:%02d", this.hour, this.minute)
        val daysString = if (this.daysOfWeek.isEmpty()) "Một lần"
        else this.daysOfWeek.sorted().joinToString(", ")
        return AlarmData(
            id = this.alarmId,
            time = timeString,
            days = daysString,
            label = this.label,
            isEnabled = this.isEnabled
        )
    }

    fun setSortType(type: SortType){
        _sortType.value = type
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(id) ?: return@launch
            // 1. Tắt lịch hẹn trong hệ thống trước
            alarmScheduler.cancel(alarm)
            // 2. Xóa dữ liệu trong DB sau
            alarmRepository.deleteAlarm(alarm)
        }
    }

    fun deleteInactiveAlarms(){
        viewModelScope.launch {alarmRepository.deleteInactiveAlarms()}
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartAlarmApp
                AlarmViewModel(app, app.container.alarmRepository, app.container.alarmScheduler)
            }
        }
    }
}
