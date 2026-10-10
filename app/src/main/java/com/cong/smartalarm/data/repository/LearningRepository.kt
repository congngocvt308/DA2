package com.cong.smartalarm.data.repository

import com.cong.smartalarm.core.database.dao.AppDao
import com.cong.smartalarm.core.database.entity.AlarmHistoryEntity
import com.cong.smartalarm.core.database.entity.AlarmSelectedQuestionEntity
import com.cong.smartalarm.core.database.entity.HistoryEntity
import com.cong.smartalarm.core.database.entity.QuestionEntity
import com.cong.smartalarm.core.database.entity.QuestionProgressEntity
import com.cong.smartalarm.core.database.entity.TopicStatsEntity

/** Dữ liệu học tập cho thuật toán SRS và màn giải câu hỏi: tiến độ, thống kê chủ đề, lịch sử trả lời. */
class LearningRepository(private val dao: AppDao) {
    // --- Nguồn câu hỏi cho một báo thức ---
    suspend fun getSelectedQuestionsForAlarmOnce(alarmId: Int): List<AlarmSelectedQuestionEntity> =
        dao.getSelectedQuestionsForAlarmOnce(alarmId)
    suspend fun getQuestionsFromLinkedTopics(alarmId: Int): List<QuestionEntity> = dao.getQuestionsFromLinkedTopics(alarmId)
    suspend fun getQuestionById(id: Int): QuestionEntity? = dao.getQuestionById(id)
    suspend fun getTopicIdByQuestionId(questionId: Int): Int? = dao.getTopicIdByQuestionId(questionId)

    // --- Tiến độ SRS ---
    suspend fun getProgressForQuestions(questionIds: List<Int>): List<QuestionProgressEntity> =
        dao.getProgressForQuestions(questionIds)
    suspend fun updateQuestionProgress(progress: QuestionProgressEntity) = dao.updateQuestionProgress(progress)
    suspend fun getTopicStats(topicId: Int): TopicStatsEntity? = dao.getTopicStats(topicId)
    suspend fun updateTopicStats(stats: TopicStatsEntity) = dao.updateTopicStats(stats)

    // --- Lịch sử ---
    suspend fun insertHistory(history: HistoryEntity) = dao.insertHistory(history)
    suspend fun insertAlarmHistory(history: AlarmHistoryEntity): Long = dao.insertAlarmHistory(history)
    suspend fun updateAlarmHistory(history: AlarmHistoryEntity) = dao.updateAlarmHistory(history)
    suspend fun getAlarmHistoryById(id: Int): AlarmHistoryEntity? = dao.getAlarmHistoryById(id)
}
