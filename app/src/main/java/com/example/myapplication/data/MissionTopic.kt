package com.example.myapplication.data

data class MissionTopic(
    val id: Int,
    val name: String,
    val questions: List<MissionQuestion>,
    var isExpanded: Boolean = false,
    var isSelected: Boolean = false
)
