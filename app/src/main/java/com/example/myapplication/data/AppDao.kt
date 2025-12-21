package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE alarmId = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Insert
    suspend fun insertTopic(topic: TopicEntity): Long

    @Query("SELECT * FROM questions WHERE ownerTopicId = :topicId")
    fun getQuestionsByTopic(topicId: Int): Flow<List<QuestionEntity>>

    @Insert
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Query("SELECT * FROM topic_stats WHERE topicId = :topicId")
    suspend fun getTopicStats(topicId: Int): TopicStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTopicStats(stats: TopicStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedQuestion(item: AlarmSelectedQuestionEntity)

    @Query("DELETE FROM alarm_selected_questions WHERE alarmId = :alarmId")
    suspend fun clearSelectedQuestionsForAlarm(alarmId: Int)

    @Query("SELECT * FROM alarm_selected_questions WHERE alarmId = :alarmId")
    fun getSelectedQuestionsForAlarm(alarmId: Int): Flow<List<AlarmSelectedQuestionEntity>>
    
    @Query("SELECT * FROM alarm_selected_questions WHERE alarmId = :alarmId")
    suspend fun getSelectedQuestionsForAlarmOnce(alarmId: Int): List<AlarmSelectedQuestionEntity>

    @Query("""
        SELECT q.* FROM questions q
        INNER JOIN alarm_selected_questions asq ON q.questionId = asq.questionId
        WHERE asq.alarmId = :alarmId
    """)
    fun getManualQuestionsForAlarm(alarmId: Int): Flow<List<QuestionEntity>>

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE isEnabled = 0")
    suspend fun deleteInactiveAlarms()

    @Query("""
        SELECT t.topicId, t.topicName, COUNT(q.questionId) AS questionCount 
        FROM topics t 
        LEFT JOIN questions q ON t.topicId = q.ownerTopicId 
        GROUP BY t.topicId
    """)
    fun getAllTopicsWithCount(): Flow<List<TopicWithCountResult>>

    @Query("SELECT * FROM questions WHERE questionId = :id")
    suspend fun getQuestionById(id: Int): QuestionEntity?

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("SELECT topicName FROM topics WHERE topicId = :id")
    suspend fun getTopicNameById(id: Int): String?

    @Query("DELETE FROM topics WHERE topicId = :id")
    suspend fun deleteTopicById(id: Int)

    @Query("UPDATE topics SET topicName = :newName WHERE topicId = :topicId")
    suspend fun updateTopicName(topicId: Int, newName: String)

    @Transaction
    @Query("SELECT * FROM topics")
    fun getTopicsWithQuestions(): Flow<List<TopicWithQuestions>>

    @Query("UPDATE alarms SET isEnabled = :enabled WHERE alarmId = :id")
    suspend fun updateAlarmEnabledStatus(id: Int, enabled: Boolean)
    
    // ==================== QR CODE METHODS ====================
    
    @Query("SELECT * FROM qr_codes ORDER BY createdAt DESC")
    fun getAllQRCodes(): Flow<List<QRCodeEntity>>
    
    @Query("SELECT * FROM qr_codes ORDER BY createdAt DESC")
    suspend fun getAllQRCodesOnce(): List<QRCodeEntity>
    
    @Query("SELECT COUNT(*) FROM qr_codes")
    suspend fun getQRCodeCount(): Int
    
    @Query("SELECT * FROM qr_codes WHERE qrId = :id")
    suspend fun getQRCodeById(id: Int): QRCodeEntity?
    
    @Query("SELECT * FROM qr_codes WHERE codeValue = :code")
    suspend fun getQRCodeByValue(code: String): QRCodeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQRCode(qrCode: QRCodeEntity): Long
    
    @Delete
    suspend fun deleteQRCode(qrCode: QRCodeEntity)
    
    @Query("DELETE FROM qr_codes WHERE qrId = :id")
    suspend fun deleteQRCodeById(id: Int)
    
    @Update
    suspend fun updateQRCode(qrCode: QRCodeEntity)
    
    // ==================== ALARM-QR LINK METHODS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmQRLink(link: AlarmQRLinkEntity)
    
    @Query("DELETE FROM alarm_qr_link WHERE alarmId = :alarmId")
    suspend fun clearQRLinksForAlarm(alarmId: Int)
    
    @Query("DELETE FROM alarm_qr_link WHERE alarmId = :alarmId AND qrId = :qrId")
    suspend fun deleteAlarmQRLink(alarmId: Int, qrId: Int)
    
    @Query("SELECT COUNT(*) FROM alarm_qr_link WHERE alarmId = :alarmId")
    suspend fun getQRLinkCountForAlarm(alarmId: Int): Int
    
    @Query("""
        SELECT q.* FROM qr_codes q
        INNER JOIN alarm_qr_link link ON q.qrId = link.qrId
        WHERE link.alarmId = :alarmId
    """)
    fun getQRCodesForAlarm(alarmId: Int): Flow<List<QRCodeEntity>>
    
    @Query("""
        SELECT q.* FROM qr_codes q
        INNER JOIN alarm_qr_link link ON q.qrId = link.qrId
        WHERE link.alarmId = :alarmId
    """)
    suspend fun getQRCodesForAlarmOnce(alarmId: Int): List<QRCodeEntity>
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM qr_codes q
            INNER JOIN alarm_qr_link link ON q.qrId = link.qrId
            WHERE link.alarmId = :alarmId AND q.codeValue = :codeValue
        )
    """)
    suspend fun isQRCodeValidForAlarm(alarmId: Int, codeValue: String): Boolean

    @Query("DELETE FROM alarm_topic_link WHERE alarmId = :alarmId")
    suspend fun clearAlarmTopicLinks(alarmId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarmTopicLink(link: AlarmTopicLink)

    // Lấy danh sách các liên kết Topic của một báo thức cụ thể
    @Query("SELECT * FROM alarm_topic_link WHERE alarmId = :alarmId")
    suspend fun getTopicLinksForAlarmOnce(alarmId: Int): List<AlarmTopicLink>
}