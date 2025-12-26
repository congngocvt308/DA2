package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val topicId: Int = 0,
    val topicName: String
)

@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(
        entity = TopicEntity::class,
        parentColumns = ["topicId"],
        childColumns = ["ownerTopicId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("ownerTopicId")]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val questionId: Int = 0,
    val ownerTopicId: Int,
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val alarmId: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String?,
    val daysOfWeek: Set<String>,
    val questionCount: Int = 0,
    val isEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val snoozeDuration: Int = 5,
    val snoozeEnabled: Boolean = false
)

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
    val topicId: Int
)

@Entity(
    tableName = "question_progress",
    foreignKeys = [ForeignKey(entity = QuestionEntity::class, parentColumns = ["questionId"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("questionId")]
)
data class QuestionProgressEntity(
    @PrimaryKey val questionId: Int,
    val correctStreak: Int = 0,         // Số lần đúng liên tiếp
    val lastReviewedDate: Date? = null,
    val nextReviewDate: Date? = null,
    val difficultyScore: Double = 1000.0, // Dùng cho hệ thống ELO
    val easinessFactor: Double = 2.5,     // Hệ số giãn cách (mặc định 2.5 của SM-2)
    val interval: Int = 0                 // Khoảng cách ngày cho lần ôn tập tới
)

@Entity(
    tableName = "topic_stats",
    foreignKeys = [ForeignKey(entity = TopicEntity::class, parentColumns = ["topicId"], childColumns = ["topicId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("topicId")]
)
data class TopicStatsEntity(
    @PrimaryKey val topicId: Int,
    val userEloScore: Double = 1000.0
)

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(entity = QuestionEntity::class, parentColumns = ["questionId"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE),
        // Thêm liên kết tới bảng AlarmHistory
        ForeignKey(entity = AlarmHistoryEntity::class, parentColumns = ["historyId"], childColumns = ["alarmHistoryId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("questionId"), Index("alarmHistoryId")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val questionId: Int,
    val alarmHistoryId: Int? = null, // Null nếu là luyện tập tự do, có giá trị nếu là lúc tắt báo thức
    val isCorrect: Boolean,
    val answeredAt: Date,
    val timeToAnswerMs: Int
)

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

@Entity(
    tableName = "alarm_selected_questions",
    foreignKeys = [
        // Chỉ giữ ForeignKey cho alarmId, bỏ ForeignKey cho questionId vì câu hỏi mặc định có ID âm
        ForeignKey(entity = AlarmEntity::class, parentColumns = ["alarmId"], childColumns = ["alarmId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("alarmId"), Index("questionId"), Index("topicId")]
)
data class AlarmSelectedQuestionEntity(
    @PrimaryKey(autoGenerate = true) val selectionId: Int = 0,
    val alarmId: Int,
    val questionId: Int,  // Có thể là ID dương (từ database) hoặc âm (câu hỏi mặc định: -1, -2, ...)
    val topicId: Int? = null
)

/**
 * Entity lưu trữ QR/Barcode
 * Người dùng có thể lưu tối đa 5 mã
 */
@Entity(tableName = "qr_codes")
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true) val qrId: Int = 0,
    val name: String,           // Tên do người dùng đặt (VD: "Mã tủ lạnh", "Mã bàn làm việc")
    val codeValue: String,      // Giá trị của mã QR/Barcode
    val codeType: String,       // "QR" hoặc "BARCODE"
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Bảng liên kết giữa Alarm và QR Code
 * Mỗi báo thức có thể sử dụng tối đa 3 mã QR/Barcode
 */
@Entity(
    tableName = "alarm_qr_link",
    primaryKeys = ["alarmId", "qrId"],
    foreignKeys = [
        ForeignKey(entity = AlarmEntity::class, parentColumns = ["alarmId"], childColumns = ["alarmId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = QRCodeEntity::class, parentColumns = ["qrId"], childColumns = ["qrId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("alarmId"), Index("qrId")]
)
data class AlarmQRLinkEntity(
    val alarmId: Int,
    val qrId: Int
)

@Entity(tableName = "UserStats")
data class UserStatsEntity(
    @PrimaryKey val userId: Int = 1, // App cá nhân thường chỉ có 1 user
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalAlarmsDismissed: Int = 0,
    val lastActiveDate: Long = 0L // Lưu timestamp ngày cuối cùng hoạt động
)