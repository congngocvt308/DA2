package com.cong.smartalarm.feature.quiz

data class QuestionData(
    val id: Int,
    val questionText: String,
    val answers: List<AnswerData>
)
