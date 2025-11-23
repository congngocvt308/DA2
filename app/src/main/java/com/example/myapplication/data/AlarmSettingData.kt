package com.example.myapplication.data

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap

data class AlarmSettingData(
    val selectedHour: Int = 8,
    val selectedMinute: Int = 10,
    val alarmName: String = "",
    val daysOfWeek: SnapshotStateMap<String, Boolean> = mutableStateMapOf(
        "CN" to false, "T2" to true, "T3" to true, "T4" to true, "T5" to true, "T6" to true, "T7" to false
    ),
    val repeatDaily: Boolean = true,
    val volume: Float = 0.7f,
    // Ví dụ: Thông báo đổ chuông sau bao lâu
    val timeUntilAlarm: String = "Đổ chuông sau 17 giờ 51 phút."
)
