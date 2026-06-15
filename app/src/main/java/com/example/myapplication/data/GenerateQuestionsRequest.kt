package com.example.myapplication.data

data class GenerateQuestionsRequest(
    val extractedText: String,
    val easyCount: Int,
    val midCount: Int,
    val hardCount: Int
)
