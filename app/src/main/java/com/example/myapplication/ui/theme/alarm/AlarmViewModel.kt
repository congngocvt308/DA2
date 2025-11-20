package com.example.myapplication.ui.theme.alarm

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.AlarmData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmViewModel : ViewModel() {
    private val _alarms = MutableStateFlow(
        listOf(
            AlarmData(1, "Hàng ngày", "06:30", "Thức dậy", true),
            AlarmData(2, "Cuối tuần", "08:00", "Đi chơi", false),
            AlarmData(3, "Thứ 2, 4, 6", "14:00", "Học Flutter", true),
            AlarmData(4, "Chủ nhật", "22:00", null, true)
        )
    )

    val alarms: StateFlow<List<AlarmData>> = _alarms.asStateFlow()

    fun addQuickAlarm(minutesToAdd: Int) {
        val now = LocalTime.now()
        val alarmTime = now.plusMinutes(minutesToAdd.toLong())
        val newAlarm = AlarmData(
            id = (System.currentTimeMillis() % 10000).toInt(),
            days = "Một lần",
            time = alarmTime.format(DateTimeFormatter.ofPattern("HH:mm")),
            label = "Báo thức nhanh",
            isEnabled = true
        )
        val currentList = _alarms.value.toMutableList()
        currentList.add(newAlarm)
        _alarms.value = currentList
    }

    fun toggleAlarm(id: Int, newState: Boolean) {
        val currentList = _alarms.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isEnabled = newState)
            _alarms.value = currentList
        }
    }
}