package com.cong.smartalarm.feature.alarmedit

import com.cong.smartalarm.core.database.entity.AlarmFaceMissionConfigEntity
import com.cong.smartalarm.feature.mission.MissionQuestion
import com.cong.smartalarm.feature.mission.MissionTopic

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
    val selectedTopicIds: List<MissionTopic> = emptyList(),
    val selectedQRCodeIds: List<Int> = emptyList(),
    val volume: Float = 0.7f,
    val faceMissionConfig: AlarmFaceMissionConfigEntity? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val timeUntilAlarm: String = ""
) {
    // Helper để lấy alarmId (nullable khi chưa lưu)
    val alarmId: Int? get() = if (id > 0) id else null
}
