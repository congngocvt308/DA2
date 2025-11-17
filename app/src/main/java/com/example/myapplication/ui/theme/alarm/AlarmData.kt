package com.example.myapplication.ui.theme.alarm

data class AlarmData(
    val id: Int,
    val time: String,
    val days: String,
    val label: String?,
    var isEnabled: Boolean
)