package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {

    // 1. Lấy tỷ lệ đúng/sai theo từng ngày (Dùng để vẽ biểu đồ đường - Line Chart)
    @Query("""
        SELECT 
            date(answeredAt/1000, 'unixepoch', 'localtime') as day,
            SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correct,
            COUNT(*) as total
        FROM history
        WHERE answeredAt > :sevenDaysAgo
        GROUP BY day 
        ORDER BY day ASC
    """)
    fun getWeeklyAccuracy(sevenDaysAgo: Long): Flow<List<DailyStat>>

    // 2. Lấy phân phối trí nhớ (Dùng để vẽ biểu đồ tròn - Pie Chart)
    // Phân loại dựa trên 'correctStreak' trong thuật toán SRS
    @Query("""
    SELECT 
        CASE 
            WHEN correctStreak = 0 THEN 'New'
            WHEN correctStreak BETWEEN 1 AND 4 THEN 'Learning'
            ELSE 'Mastered'
        END as status,
        COUNT(*) as count
    FROM question_progress
    GROUP BY status
    """)
    fun getSrsDistribution(): Flow<List<SrsStat>>

    // 3. Lấy 5 lần báo thức gần nhất để tính toán Wake-up Score (Chỉ số tỉnh táo)
    @Query("SELECT * FROM alarm_history ORDER BY firstRingTime DESC LIMIT 5")
    suspend fun getRecentAlarmHistory(): List<AlarmHistoryEntity>

    @Query("""
    SELECT q.* FROM questions q
    INNER JOIN question_progress p ON q.questionId = p.questionId
    WHERE (CASE 
        WHEN p.correctStreak = 0 THEN 'New'
        WHEN p.correctStreak < 5 THEN 'Learning'
        ELSE 'Mastered'
    END) = :status
    """)
    fun getQuestionsBySrsStatus(status: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM UserStats WHERE userId = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun initializeUserStats(stats: UserStatsEntity)

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)

    @Query("SELECT * FROM UserStats WHERE userId = 1")
    suspend fun getUserStatsSynchronous(): UserStatsEntity?

    @Query("""
    UPDATE alarm_history 
    SET snoozeCount = snoozeCount + 1 
    WHERE alarmId = :alarmId AND isDismissed = 0
    """)
    suspend fun incrementSnoozeCount(alarmId: Int)
}