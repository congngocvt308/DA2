package com.cong.smartalarm.data.repository

import com.cong.smartalarm.core.database.dao.StatisticsDao
import com.cong.smartalarm.core.database.entity.AlarmHistoryEntity
import com.cong.smartalarm.core.database.entity.QuestionEntity
import com.cong.smartalarm.core.database.entity.UserStatsEntity
import com.cong.smartalarm.core.database.model.DailyStat
import com.cong.smartalarm.core.database.model.SrsStat
import kotlinx.coroutines.flow.Flow

/** Số liệu cho màn Thống kê: tỉ lệ đúng theo ngày, phân phối SRS, điểm và chuỗi ngày. */
class StatsRepository(private val dao: StatisticsDao) {
    fun getWeeklyAccuracy(sinceMillis: Long): Flow<List<DailyStat>> = dao.getWeeklyAccuracy(sinceMillis)
    fun getSrsDistribution(): Flow<List<SrsStat>> = dao.getSrsDistribution()
    fun getQuestionsBySrsStatus(status: String): Flow<List<QuestionEntity>> = dao.getQuestionsBySrsStatus(status)
    suspend fun getRecentAlarmHistory(): List<AlarmHistoryEntity> = dao.getRecentAlarmHistory()

    fun getUserStats(): Flow<UserStatsEntity?> = dao.getUserStats()
    suspend fun getUserStatsSynchronous(): UserStatsEntity? = dao.getUserStatsSynchronous()
    suspend fun initializeUserStats(stats: UserStatsEntity) = dao.initializeUserStats(stats)
    suspend fun updateUserStats(stats: UserStatsEntity) = dao.updateUserStats(stats)
    suspend fun incrementSnoozeCount(alarmId: Int) = dao.incrementSnoozeCount(alarmId)
}
