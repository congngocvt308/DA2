package com.example.myapplication.ui.theme.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.alarm_logic.AlarmScheduler
import com.example.myapplication.data.AlarmData
import com.example.myapplication.data.AlarmEntity
import com.example.myapplication.data.AlarmSettingData
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration
import java.util.Calendar

enum class SortType{ DEFAULT, ACTIVE_FIRST}

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.getDatabase(application).appDao()
    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    private val tickerFlow = flow {
        while (true) {
            emit(LocalDateTime.now()) // Bắn ra thời gian hiện tại
            delay(60000) // Đợi 60 giây rồi lặp lại
        }
    }

    val timeUntilNextAlarms: StateFlow<String> = combine(
        alarmDao.getAllAlarms(), // Luồng 1: Dữ liệu DB
        tickerFlow               // Luồng 2: Thời gian trôi
    ) { entities, now ->         // 'now' được lấy từ tickerFlow

        // Lọc các báo thức đang bật
        val activeAlarms = entities.filter { it.isEnabled }

        if (activeAlarms.isEmpty()) {
            "Không có báo thức sắp tới"
        } else {
            // Tính thời gian reo của TẤT CẢ báo thức đang bật
            val nextRingTimes = activeAlarms.map { alarm ->
                findNextAlarmTime(
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
    }.stateIn(
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
                if (hours == 0L && minutes == 0L) "Đổ chuông trong vòng chưa đầy 1 phút"
                else "Báo thức tiếp theo sau $hours giờ $minutes phút"
            }
            days == 1L -> "Báo thức tiếp theo sau 1 ngày"
            else -> "Báo thức tiếp theo sau $days ngày"
        }
    }

    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    val alarms: StateFlow<List<AlarmData>> = combine(
        alarmDao.getAllAlarms(),
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


    fun toggleAlarm(alarmId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val oldAlarm = alarmDao.getAlarmById(alarmId)
            if (oldAlarm != null) {
                val updatedAlarm = oldAlarm.copy(isEnabled = isEnabled)
                alarmDao.updateAlarm(updatedAlarm)
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

    fun findNextAlarmTime(
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
            val candidateDate = target.plusDays(i.toLong())
            val dayCode = getDayCode(candidateDate.dayOfWeek)
            if (daysOfWeek.contains(dayCode)) {
                if (i == 0 && candidateDate.isBefore(now)) {
                    continue
                }
                return candidateDate
            }
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

    fun deleteAlarm(alarmId: Int) {
        viewModelScope.launch {
            val alarm = alarmDao.getAlarmById(alarmId)
            if (alarm != null) {
                alarmDao.deleteAlarm(alarm)
            }
        }
    }

    fun deleteInactiveAlarms(){
        viewModelScope.launch {alarmDao.deleteInactiveAlarms()}
    }
}