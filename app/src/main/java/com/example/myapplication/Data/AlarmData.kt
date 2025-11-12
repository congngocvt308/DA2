package com.example.myapplication.Data

data class AlarmData(
    val id: Int,
    val time: String,
    val days: String,
    val label: String?,
    var isEnabled: Boolean
)