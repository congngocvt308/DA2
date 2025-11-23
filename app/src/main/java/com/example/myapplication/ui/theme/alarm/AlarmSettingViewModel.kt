package com.example.myapplication.ui.theme.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AlarmSettingData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmSettingViewModel(private val alarmId: Int) : ViewModel() {
    private val _state = MutableStateFlow(AlarmSettingData())
    val state: StateFlow<AlarmSettingData> = _state.asStateFlow()
    init {
        if (alarmId != -1) { // Giả sử -1 là báo thức mới
            loadAlarmSettings()
        }
    }

    private fun loadAlarmSettings() {
        viewModelScope.launch {
            // TODO: Logic gọi Repository để lấy dữ liệu báo thức theo alarmId
            delay(500) // Mô phỏng độ trễ tải dữ liệu

            // Cập nhật State với dữ liệu đã tải (ví dụ: báo thức lúc 9:30)
            _state.value = _state.value.copy(
                selectedHour = 9,
                selectedMinute = 30,
                alarmName = "Báo thức buổi sáng",
                timeUntilAlarm = "Đổ chuông sau 16 giờ 00 phút."
            )
        }
    }

    // --- Events (Logic xử lý) ---

    fun updateHour(newHour: Int) {
        _state.value = _state.value.copy(selectedHour = newHour)
        // TODO: Cập nhật timeUntilAlarm
    }

    fun updateMinute(newMinute: Int) {
        _state.value = _state.value.copy(selectedMinute = newMinute)
        // TODO: Cập nhật timeUntilAlarm
    }

    fun updateAlarmName(newName: String) {
        _state.value = _state.value.copy(alarmName = newName)
    }

    fun toggleRepeatDaily(isRepeat: Boolean) {
        _state.value = _state.value.copy(repeatDaily = isRepeat)
        // Nếu chuyển sang Hàng ngày, có thể chọn/bỏ chọn tất cả các ngày
    }

    fun toggleDay(day: String) {
        val currentDays = _state.value.daysOfWeek
        currentDays[day] = !(currentDays[day] ?: false)
    }

    fun updateVolume(newVolume: Float) {
        _state.value = _state.value.copy(volume = newVolume)
    }

    fun saveAlarm() {
        // TODO: Logic lưu báo thức vào Database/Repository
        // Dữ liệu cần lưu là _state.value
        println("Saving Alarm ID $alarmId with settings: ${_state.value}")
    }
}