package com.cong.smartalarm.data.repository

import com.cong.smartalarm.core.database.dao.AppDao
import com.cong.smartalarm.core.database.entity.AlarmQRLinkEntity
import com.cong.smartalarm.core.database.entity.QRCodeEntity
import kotlinx.coroutines.flow.Flow

/** Mã QR / Barcode đã đăng ký và liên kết của chúng với báo thức. */
class QRCodeRepository(private val dao: AppDao) {
    fun getAllQRCodes(): Flow<List<QRCodeEntity>> = dao.getAllQRCodes()
    suspend fun getQRCodeCount(): Int = dao.getQRCodeCount()
    suspend fun getQRCodeByValue(code: String): QRCodeEntity? = dao.getQRCodeByValue(code)
    suspend fun insertQRCode(qrCode: QRCodeEntity): Long = dao.insertQRCode(qrCode)
    suspend fun updateQRCode(qrCode: QRCodeEntity) = dao.updateQRCode(qrCode)
    suspend fun deleteQRCode(qrCode: QRCodeEntity) = dao.deleteQRCode(qrCode)

    fun getQRCodesForAlarm(alarmId: Int): Flow<List<QRCodeEntity>> = dao.getQRCodesForAlarm(alarmId)
    suspend fun getQRCodesForAlarmOnce(alarmId: Int): List<QRCodeEntity> = dao.getQRCodesForAlarmOnce(alarmId)
    suspend fun getQRLinkCountForAlarm(alarmId: Int): Int = dao.getQRLinkCountForAlarm(alarmId)
    suspend fun insertAlarmQRLink(link: AlarmQRLinkEntity) = dao.insertAlarmQRLink(link)
    suspend fun deleteAlarmQRLink(alarmId: Int, qrId: Int) = dao.deleteAlarmQRLink(alarmId, qrId)
    suspend fun isQRCodeValidForAlarm(alarmId: Int, codeValue: String): Boolean =
        dao.isQRCodeValidForAlarm(alarmId, codeValue)
}
