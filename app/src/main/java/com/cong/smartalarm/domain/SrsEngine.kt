package com.cong.smartalarm.domain

import com.cong.smartalarm.core.database.entity.HistoryEntity
import com.cong.smartalarm.core.database.entity.QuestionEntity
import com.cong.smartalarm.core.database.entity.QuestionProgressEntity
import com.cong.smartalarm.core.database.entity.TopicStatsEntity
import com.cong.smartalarm.data.repository.LearningRepository
import com.cong.smartalarm.feature.mission.MissionQuestion
import java.util.Date

/**
 * Thuật toán lặp lại ngắt quãng (SRS) chọn câu hỏi khi báo thức reo và cập nhật tiến độ sau mỗi câu trả lời.
 * Phần tính toán thuần (điểm ưu tiên, tiến độ kế tiếp, điểm Elo) nằm trong [SrsMath] để kiểm thử độc lập.
 */
class SrsEngine(private val repository: LearningRepository) {

    // --- PHẦN 1: CHỌN CÂU HỎI KHI BÁO THỨC REO ---
    suspend fun generateMissionQuestions(alarmId: Int, countNeeded: Int): List<MissionQuestion> {
        // 1. Gom bể câu hỏi: câu chọn tay + câu thuộc các chủ đề gắn với báo thức
        val manualQuestions = repository.getSelectedQuestionsForAlarmOnce(alarmId)
            .mapNotNull { repository.getQuestionById(it.questionId) }
        val topicQuestions = repository.getQuestionsFromLinkedTopics(alarmId)
        val rawPool = (manualQuestions + topicQuestions).distinctBy { it.questionId }
        if (rawPool.isEmpty()) return emptyList()

        // 2. Lấy dữ liệu SRS
        val progressMap = repository.getProgressForQuestions(rawPool.map { it.questionId })
            .associateBy { it.questionId }

        // 3. Sắp xếp ưu tiên (đến hạn ôn trước, rồi câu khó), hoà điểm thì ngẫu nhiên
        val now = System.currentTimeMillis()
        val sortedList = rawPool.sortedWith(
            compareByDescending<QuestionEntity> { SrsMath.priorityScore(progressMap[it.questionId], now) }
                .thenBy { Math.random() }
        )

        // 4. Lấy N câu đầu
        return sortedList.take(countNeeded).map { q ->
            MissionQuestion(id = q.questionId, text = q.prompt, isSelected = false)
        }
    }

    // --- PHẦN 2: CẬP NHẬT KẾT QUẢ (SAU KHI TRẢ LỜI) ---
    suspend fun processAnswer(
        questionId: Int,
        isCorrect: Boolean,
        timeSpentMs: Long,
        alarmHistoryId: Int?
    ) {
        val now = System.currentTimeMillis()
        repository.insertHistory(
            HistoryEntity(
                questionId = questionId,
                alarmHistoryId = alarmHistoryId,
                isCorrect = isCorrect,
                answeredAt = Date(now),
                timeToAnswerMs = timeSpentMs.toInt()
            )
        )

        val current = repository.getProgressForQuestions(listOf(questionId)).firstOrNull()
            ?: QuestionProgressEntity(questionId = questionId)
        repository.updateQuestionProgress(SrsMath.nextProgress(current, isCorrect, now))

        // Chỉ tính điểm chủ đề nếu câu hỏi thuộc về một chủ đề
        val topicId = repository.getTopicIdByQuestionId(questionId) ?: return
        val stats = repository.getTopicStats(topicId) ?: TopicStatsEntity(topicId = topicId)
        repository.updateTopicStats(stats.copy(userEloScore = SrsMath.nextEloScore(stats.userEloScore, isCorrect)))
    }
}

/** Các phép tính thuần của SRS — không đụng database, dễ kiểm thử. */
object SrsMath {
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    /** Câu mới: 500. Câu đến hạn: 1000 + thời gian quá hạn. Chưa đến hạn: độ khó của câu. */
    fun priorityScore(progress: QuestionProgressEntity?, now: Long): Double {
        if (progress == null) return 500.0
        val nextReviewTime = progress.nextReviewDate?.time ?: 0L
        return if (nextReviewTime <= now) 1000.0 + (now - nextReviewTime) else progress.difficultyScore
    }

    /**
     * Đúng: chuỗi đúng +1, hệ số dễ +0.1, khoảng cách ôn = 1 ngày rồi nhân theo hệ số dễ.
     * Sai: chuỗi về 0, hệ số dễ -0.2 (tối thiểu 1.3), ôn lại sau 1 ngày.
     */
    fun nextProgress(progress: QuestionProgressEntity, isCorrect: Boolean, now: Long): QuestionProgressEntity {
        val updated = if (isCorrect) {
            progress.copy(
                correctStreak = progress.correctStreak + 1,
                easinessFactor = progress.easinessFactor + 0.1,
                interval = if (progress.interval == 0) 1 else (progress.interval * progress.easinessFactor).toInt()
            )
        } else {
            progress.copy(
                correctStreak = 0,
                easinessFactor = (progress.easinessFactor - 0.2).coerceAtLeast(1.3),
                interval = 1
            )
        }
        return updated.copy(
            lastReviewedDate = Date(now),
            nextReviewDate = Date(now + updated.interval * DAY_MILLIS)
        )
    }

    /** Elo đơn giản: đúng +10, sai -5 (không âm). */
    fun nextEloScore(score: Double, isCorrect: Boolean): Double =
        if (isCorrect) score + 10.0 else (score - 5.0).coerceAtLeast(0.0)
}
