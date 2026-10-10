package com.cong.smartalarm.feature.mission

data class MissionTopic(
    val id: Int,
    val name: String,
    val questions: List<MissionQuestion>,
    var isExpanded: Boolean = false,
    var isSelected: Boolean = false
)
