package com.example.myapplication.data

data class MissionQuestion(
    val id: String,
    val text: String,
    var isSelected: Boolean = false
)