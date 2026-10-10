package com.cong.smartalarm.feature.mission

data class MissionQuestion(
    val id: Int,
    val text: String,
    val isSelected: Boolean = false
)