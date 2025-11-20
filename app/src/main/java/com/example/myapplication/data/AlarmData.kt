package com.example.myapplication.data

data class AlarmData(
    val id: Int,
    val days: String,
    val time: String,
    val label: String?,
    var isEnabled: Boolean
)