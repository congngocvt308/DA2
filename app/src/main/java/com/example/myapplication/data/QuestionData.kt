package com.cong.smartalarm.data

data class QuestionData(
    val id: Int,
    val questionText: String,
    val answers: List<AnswerData>
)
