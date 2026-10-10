package com.cong.smartalarm.data.repository

import com.cong.smartalarm.core.database.dao.AppDao
import com.cong.smartalarm.core.database.entity.AlarmEntity
import com.cong.smartalarm.core.database.entity.AlarmFaceMissionConfigEntity
import com.cong.smartalarm.core.database.entity.AlarmQRLinkEntity
import com.cong.smartalarm.core.database.entity.AlarmSelectedQuestionEntity
import com.cong.smartalarm.core.database.entity.AlarmTopicLink
import com.cong.smartalarm.core.database.entity.QRCodeEntity
import kotlinx.coroutines.flow.Flow

/** Báo thức và cấu hình nhiệm vụ gắn với từng báo thức (câu hỏi, chủ đề, mã QR, nụ cười). */
class AlarmRepository(private val dao: AppDao) {

    // --- Báo thức ---
    fun getAllAlarms(): Flow<List<AlarmEntity>> = dao.getAllAlarms()
    suspend fun getEnabledAlarms(): List<AlarmEntity> = dao.getEnabledAlarms()
    suspend fun getAlarmById(id: Int): AlarmEntity? = dao.getAlarmById(id)
    suspend fun insertAlarm(alarm: AlarmEntity): Long = dao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: AlarmEntity) = dao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: AlarmEntity) = dao.deleteAlarm(alarm)
    suspend fun deleteInactiveAlarms() = dao.deleteInactiveAlarms()
    suspend fun updateAlarmEnabledStatus(id: Int, enabled: Boolean) = dao.updateAlarmEnabledStatus(id, enabled)

    // --- Câu hỏi chọn tay cho báo thức ---
    suspend fun clearSelectedQuestionsForAlarm(alarmId: Int) = dao.clearSelectedQuestionsForAlarm(alarmId)
    suspend fun insertSelectedQuestion(item: AlarmSelectedQuestionEntity) = dao.insertSelectedQuestion(item)
    suspend fun getSelectedQuestionsForAlarmOnce(alarmId: Int): List<AlarmSelectedQuestionEntity> =
        dao.getSelectedQuestionsForAlarmOnce(alarmId)

    // --- Chủ đề gắn với báo thức ---
    suspend fun clearAlarmTopicLinks(alarmId: Int) = dao.clearAlarmTopicLinks(alarmId)
    suspend fun insertAlarmTopicLink(link: AlarmTopicLink) = dao.insertAlarmTopicLink(link)
    suspend fun getTopicLinksForAlarmOnce(alarmId: Int): List<AlarmTopicLink> = dao.getTopicLinksForAlarmOnce(alarmId)

    // --- Mã QR gắn với báo thức ---
    suspend fun clearQRLinksForAlarm(alarmId: Int) = dao.clearQRLinksForAlarm(alarmId)
    suspend fun insertAlarmQRLink(link: AlarmQRLinkEntity) = dao.insertAlarmQRLink(link)
    suspend fun getQRCodesForAlarmOnce(alarmId: Int): List<QRCodeEntity> = dao.getQRCodesForAlarmOnce(alarmId)
    suspend fun getQRLinkCountForAlarm(alarmId: Int): Int = dao.getQRLinkCountForAlarm(alarmId)

    // --- Cấu hình thử thách nụ cười ---
    suspend fun getFaceMissionConfigByAlarmId(alarmId: Int): AlarmFaceMissionConfigEntity? =
        dao.getFaceMissionConfigByAlarmId(alarmId)
    suspend fun insertOrUpdateFaceMissionConfig(config: AlarmFaceMissionConfigEntity): Long =
        dao.insertOrUpdateFaceMissionConfig(config)
    suspend fun deleteFaceMissionConfigByAlarmId(alarmId: Int) = dao.deleteFaceMissionConfigByAlarmId(alarmId)
}
