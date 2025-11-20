package com.example.myapplication.ui.theme.navigation

object Screen {
    const val ALARM_TAB = "alarm"
    const val TOPIC_TAB = "topic"
    const val STATS_TAB = "stats"
    const val ALARM_SETTINGS = "alarm_settings/{alarmId}"
    fun alarmSettingsRoute(alarmId: Int) = "alarm_settings/$alarmId"
}