package com.example.myapplication.data

data class AlarmSettingData(
    val id: Int = -1,
    val hour: Int = 6,
    val minute: Int = 30,
    val label: String = "",
    val daysOfWeek: Set<String> = emptySet(),
    val isSnoozeEnabled: Boolean = false,
    val snoozeDuration: Int = 5,
    val ringtoneUri: String = "",
    val questionCount: Int = 0,
    val selectedQuestions: List<MissionQuestion> = emptyList(),
    val volume: Float = 0.7f,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val timeUntilAlarm: String = ""
)
