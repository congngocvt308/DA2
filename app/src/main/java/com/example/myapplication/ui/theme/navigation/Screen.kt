package com.example.myapplication.ui.theme.navigation

object Screen {
    const val ALARM_TAB = "alarm"
    const val TOPIC_TAB = "topic"
    const val STATS_TAB = "stats"
    const val QUIZ_SCREEN = "quiz_screen"
    const val MISSION_SELECTION = "mission_selection"
    const val ALARM_RINGING = "alarm_ringing"

    const val TOPIC_DETAIL = "topic_detail/{topicId}"
    fun topicDetailRoute(topicId: Int) = "topic_detail/$topicId"

    const val ALARM_SETTINGS = "alarm_settings/{alarmId}"
    fun alarmSettingsRoute(alarmId: Int) = "alarm_settings/$alarmId"
}