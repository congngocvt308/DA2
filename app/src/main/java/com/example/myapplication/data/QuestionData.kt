package com.example.myapplication.data

data class QuestionData(
    val id: Int,
    val questionText: String,
    val answers: List<AnswerData>
)
