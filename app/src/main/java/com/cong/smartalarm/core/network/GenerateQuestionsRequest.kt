package com.cong.smartalarm.core.network

data class GenerateQuestionsRequest(
    val extractedText: String,
    val easyCount: Int,
    val midCount: Int,
    val hardCount: Int
)
