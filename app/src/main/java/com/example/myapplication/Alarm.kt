package com.example.myapplication

data class Alarm(
    val id: Int,
    val time: String,
    val days: String,
    val label: String?,
    var isEnabled: Boolean
)