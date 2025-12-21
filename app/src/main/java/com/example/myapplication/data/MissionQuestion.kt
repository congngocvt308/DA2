package com.example.myapplication.data

data class MissionQuestion(
    val id: Int,
    val text: String,
    val isSelected: Boolean = false
)