package com.cong.smartalarm.feature.mission

data class MissionUiState(
    val topics: List<MissionTopic> = emptyList(),
    val questionCount: Int = 0,
    val isLoading: Boolean = true
)