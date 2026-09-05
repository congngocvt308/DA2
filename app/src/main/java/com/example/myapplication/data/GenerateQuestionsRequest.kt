package com.cong.smartalarm.data

data class GenerateQuestionsRequest(
    val extractedText: String,
    val easyCount: Int,
    val midCount: Int,
    val hardCount: Int
)
