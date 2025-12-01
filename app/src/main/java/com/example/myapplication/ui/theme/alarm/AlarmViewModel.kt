package com.example.myapplication.ui.theme.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AlarmData
import com.example.myapplication.data.AlarmEntity
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Duration

enum class SortType{ DEFAULT, ACTIVE_FIRST}

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.getDatabase(application).appDao()
    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    val timeUntilNextAlarms: StateFlow<String> = alarmDao.getAllAlarms().map { entities ->
        val activeAlarms = entities.filter { it.isEnabled}
        if(activeAlarms.isEmpty()) "Không có báo thức sắp tới"
        else{
            val now = LocalDateTime.now()
            val nextRingTimes = activeAlarms.map{ alarm ->
                findNextRingTime(now, alarm.hour, alarm.minute, alarm.daysOfWeek)
            }
            val nearestTime = nextRingTimes.minOrNull()
            if(nearestTime !=null){
                val duration = Duration.between(now, nearestTime)
                formatDuration(duration)
            } else "Không có báo thức sắp tới"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),"Đang tính toán...")
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

    fun addQuickAlarm(minutesToAdd: Int) {
        viewModelScope.launch {
            val now = LocalTime.now().plusMinutes(minutesToAdd.toLong())
            val newEntity = AlarmEntity(
                hour = now.hour,
                minute = now.minute,
                label = "Báo thức nhanh",
                daysOfWeek = emptySet(),
                questionCount = 3,
                isEnabled = true
            )
            alarmDao.insertAlarm(newEntity)
        }
    }

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

    private fun findNextRingTime(now: LocalDateTime, hour: Int, minute: Int, daysOfWeek: Set<String>): LocalDateTime{
        var target = now.withHour(hour).withNano(0)
        if(daysOfWeek.isEmpty()){
            if(target.isBefore(now)||target.isEqual(now)){
                target = target.plusDays(1)
            }
            return target
        }
        for(i in 0..7){
            val dayCode = getDayCode(target.dayOfWeek)
            if(daysOfWeek.contains(dayCode)&&target.isAfter(now)){
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

    private fun formatDuration(duration: Duration): String{
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        return when{
            days >=1 -> "Đổ chuông sau $days ngày"
            hours == 0L && minutes == 0L -> "Đổ chuông trong vòng chưa đầy 1 phút"
            else -> "Đổ chuông sau $hours giờ $minutes phút"
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