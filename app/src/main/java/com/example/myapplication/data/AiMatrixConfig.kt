package com.cong.smartalarm.data

data class AiMatrixConfig(
    val suggestedTopic: String,
    val easyCount: Float = 3f,
    val midCount: Float = 1f,
    val hardCount: Float = 0f
)
