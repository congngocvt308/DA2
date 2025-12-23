package com.example.myapplication.data

data class QuizUiStateData(
    val questionPool: List<QuestionData> = emptyList(),
    val poolIndex: Int = 0,
    val targetCorrectAnswers: Int = 0,
    val correctlyAnsweredCount: Int = 0,
    val selectedAnswerId: Int? = null,
    val isAnswered: Boolean = false,
    val isFinished: Boolean = false,
    val timerProgress: Float = 1f,
    val isTimeOut: Boolean = false,
    val isLoading: Boolean = true
)
