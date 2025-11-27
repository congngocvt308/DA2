package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- ALARM ---
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE alarmId = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    // --- TOPIC ---
    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Insert
    suspend fun insertTopic(topic: TopicEntity): Long

    // --- QUESTION ---
    @Query("SELECT * FROM questions WHERE ownerTopicId = :topicId")
    fun getQuestionsByTopic(topicId: Int): Flow<List<QuestionEntity>>

    @Insert
    suspend fun insertQuestion(question: QuestionEntity): Long

    // --- STATS & LINK ---
    @Insert
    suspend fun insertAlarmTopicLink(link: AlarmTopicLink)

    @Query("SELECT * FROM topic_stats WHERE topicId = :topicId")
    suspend fun getTopicStats(topicId: Int): TopicStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateTopicStats(stats: TopicStatsEntity)
}