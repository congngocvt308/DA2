package com.cong.smartalarm.feature.quiz

data class AnswerData(
    val id: Int,
    val text: String,
    val isCorrect: Boolean = false
)
