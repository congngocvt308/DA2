package com.cong.smartalarm.feature.alarmlist

data class AlarmData(
    val id: Int,
    val days: String,
    val time: String,
    val label: String?,
    var isEnabled: Boolean
)