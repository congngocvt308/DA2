package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

// 1. Bảng Topic
@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val topicId: Int = 0,
    val topicName: String
)

// 2. Bảng Question
@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(
        entity = TopicEntity::class,
        parentColumns = ["topicId"],
        childColumns = ["ownerTopicId"],
        onDelete = ForeignKey.CASCADE // Xóa chủ đề -> Xóa luôn câu hỏi
    )],
    indices = [Index("ownerTopicId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val questionId: Int = 0,
    val ownerTopicId: Int,
    val prompt: String,
    val options: List<String>, // Converter sẽ xử lý cái này
    val correctAnswer: String
)

// 3. Bảng Alarm
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val alarmId: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String?,
    val daysOfWeek: Set<String>, // Converter sẽ xử lý cái này
    val questionCount: Int = 3,
    val isEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val snoozeDuration: Int = 5,
    val snoozeEnabled: Boolean = false
)

// 4. Bảng AlarmTopicLink (Quan hệ N-N)
@Entity(
    tableName = "alarm_topic_link",
    primaryKeys = ["alarmId", "topicId"],
    foreignKeys = [
        ForeignKey(entity = AlarmEntity::class, parentColumns = ["alarmId"], childColumns = ["alarmId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TopicEntity::class, parentColumns = ["topicId"], childColumns = ["topicId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("alarmId"), Index("topicId")]
)
data class AlarmTopicLink(
    val alarmId: Int,
    val topicId: Int,
    val isSelectAll: Boolean = true
)

// 5. Bảng QuestionProgress (1-1 với Question)
@Entity(
    tableName = "question_progress",
    foreignKeys = [ForeignKey(entity = QuestionEntity::class, parentColumns = ["questionId"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("questionId")]
)
data class QuestionProgressEntity(
    @PrimaryKey val questionId: Int,
    val correctStreak: Int = 0,
    val lastReviewedDate: Date? = null,
    val nextReviewDate: Date? = null,
    val difficultyScore: Double = 1000.0
)

// 6. Bảng TopicStats (1-1 với Topic)
@Entity(
    tableName = "topic_stats",
    foreignKeys = [ForeignKey(entity = TopicEntity::class, parentColumns = ["topicId"], childColumns = ["topicId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("topicId")]
)
data class TopicStatsEntity(
    @PrimaryKey val topicId: Int,
    val userEloScore: Double = 1000.0
)

// 7. Bảng History (Lịch sử trả lời)
@Entity(
    tableName = "history",
    foreignKeys = [ForeignKey(entity = QuestionEntity::class, parentColumns = ["questionId"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("questionId")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val questionId: Int,
    val isCorrect: Boolean,
    val answeredAt: Date,
    val timeToAnswerMs: Int
)

// 8. Bảng AlarmHistory (Lịch sử báo thức)
@Entity(
    tableName = "alarm_history",
    foreignKeys = [ForeignKey(entity = AlarmEntity::class, parentColumns = ["alarmId"], childColumns = ["alarmId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("alarmId")]
)
data class AlarmHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val alarmId: Int,
    val snoozeCount: Int = 0,
    val scheduledTime: Date,
    val firstRingTime: Date,
    val dismissalTime: Date?,
    val isDismissed: Boolean = false
)

// 9. Bảng AlarmSelectedQuestion (MỚI: Lưu các câu hỏi được chọn riêng lẻ)
@Entity(
    tableName = "alarm_selected_questions",
    foreignKeys = [
        ForeignKey(entity = AlarmEntity::class, parentColumns = ["alarmId"], childColumns = ["alarmId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = QuestionEntity::class, parentColumns = ["questionId"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TopicEntity::class, parentColumns = ["topicId"], childColumns = ["topicId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("alarmId"), Index("questionId"), Index("topicId")]
)
data class AlarmSelectedQuestionEntity(
    @PrimaryKey(autoGenerate = true) val selectionId: Int = 0,
    val alarmId: Int,
    val questionId: Int,
    val topicId: Int? = null // Cho phép null nếu DBML của bạn không bắt buộc, hoặc Int nếu bắt buộc
)