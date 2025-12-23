package com.example.myapplication.logic

import com.example.myapplication.data.AppDao
import com.example.myapplication.data.HistoryEntity
import com.example.myapplication.data.MissionQuestion
import com.example.myapplication.data.QuestionEntity
import com.example.myapplication.data.QuestionProgressEntity
import com.example.myapplication.data.TopicStatsEntity
import java.util.Date

class QuestionAlgorithmManager(private val dao: AppDao) {

    // --- PHẦN 1: CHỌN CÂU HỎI KHI BÁO THỨC REO ---
    suspend fun generateMissionQuestions(alarmId: Int, countNeeded: Int): List<MissionQuestion> {
        // 1. GOM BỂ CÂU HỎI
        val manualQuestions = dao.getSelectedQuestionsForAlarmOnce(alarmId)
            .mapNotNull { dao.getQuestionById(it.questionId) }

        val topicQuestions = dao.getQuestionsFromLinkedTopics(alarmId)

        val rawPool = (manualQuestions + topicQuestions).distinctBy { it.questionId }

        if (rawPool.isEmpty()) return emptyList()

        // 2. LẤY DỮ LIỆU SRS
        val progressMap = dao.getProgressForQuestions(rawPool.map { it.questionId })
            .associateBy { it.questionId }

        // 3. SẮP XẾP ƯU TIÊN
        val now = System.currentTimeMillis()

        val sortedList = rawPool.sortedWith(compareByDescending<QuestionEntity> { question ->
            val progress = progressMap[question.questionId]

            var score = 0.0

            if (progress == null) {
                score = 500.0
            } else {
                // SỬA LỖI 1: Chuyển Date? sang Long để so sánh
                // progress.nextReviewDate?.time ?: 0L : Lấy thời gian, nếu null thì coi là 0
                val nextReviewTime = progress.nextReviewDate?.time ?: 0L
                val isDue = nextReviewTime <= now

                if (isDue) {
                    // SỬA LỖI 2: Trừ Long cho Long (now - nextReviewTime)
                    score = 1000.0 + (now - nextReviewTime)
                } else {
                    score = progress.difficultyScore
                }
            }
            score
        }.thenBy { Math.random() })

        // 4. LẤY TOP N CÂU
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
        // A. Lưu Lịch sử (History)
        dao.insertHistory(HistoryEntity(
            questionId = questionId,
            alarmHistoryId = alarmHistoryId,
            isCorrect = isCorrect,
            // SỬA LỖI 3: Chuyển Long sang Date
            answeredAt = Date(System.currentTimeMillis()),
            timeToAnswerMs = timeSpentMs.toInt()
        ))

        // B. Cập nhật SRS
        var progress = dao.getProgressForQuestions(listOf(questionId)).firstOrNull()
            ?: QuestionProgressEntity(questionId = questionId)

        if (isCorrect) {
            progress = progress.copy(
                correctStreak = progress.correctStreak + 1,
                easinessFactor = progress.easinessFactor + 0.1,
                interval = if (progress.interval == 0) 1 else (progress.interval * progress.easinessFactor).toInt()
            )
        } else {
            progress = progress.copy(
                correctStreak = 0,
                easinessFactor = (progress.easinessFactor - 0.2).coerceAtLeast(1.3),
                interval = 1
            )
        }

        // Tính thời gian review tiếp theo (dạng Long)
        val nextReviewTimeLong = System.currentTimeMillis() + (progress.interval * 24 * 60 * 60 * 1000L)

        // SỬA LỖI 4: Chuyển Long sang Date khi lưu vào Entity
        progress = progress.copy(
            lastReviewedDate = Date(System.currentTimeMillis()),
            nextReviewDate = Date(nextReviewTimeLong)
        )
        dao.updateQuestionProgress(progress)
        val topicId = dao.getTopicIdByQuestionId(questionId)

        // Chỉ tính điểm nếu câu hỏi thuộc về 1 Topic cụ thể (không phải null)
        if (topicId != null) {
            var stats = dao.getTopicStats(topicId) ?: TopicStatsEntity(topicId = topicId)

            // Logic tính điểm ELO đơn giản:
            // Đúng: +10 điểm, Sai: -5 điểm (không thấp hơn 0)
            val newScore = if (isCorrect) {
                stats.userEloScore + 10.0
            } else {
                (stats.userEloScore - 5.0).coerceAtLeast(0.0)
            }

            dao.updateTopicStats(stats.copy(userEloScore = newScore))
        }
    }
}