package com.cong.smartalarm.domain

import com.cong.smartalarm.core.database.entity.QuestionProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class SrsMathTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_800_000_000_000L

    @Test
    fun newQuestionHasBaselinePriority() {
        assertEquals(500.0, SrsMath.priorityScore(null, now), 0.0)
    }

    @Test
    fun overdueQuestionRanksByHowLateItIs() {
        val due = QuestionProgressEntity(questionId = 1, nextReviewDate = Date(now - 1_000))
        val veryLate = QuestionProgressEntity(questionId = 2, nextReviewDate = Date(now - day))
        assertEquals(2000.0, SrsMath.priorityScore(due, now), 0.0) // 1000 + 1000 ms quá hạn
        assertTrue(SrsMath.priorityScore(veryLate, now) > SrsMath.priorityScore(due, now))
    }

    @Test
    fun notYetDueQuestionUsesDifficulty() {
        val later = QuestionProgressEntity(questionId = 1, nextReviewDate = Date(now + day), difficultyScore = 321.0)
        assertEquals(321.0, SrsMath.priorityScore(later, now), 0.0)
    }

    @Test
    fun firstCorrectAnswerSchedulesReviewTomorrow() {
        val next = SrsMath.nextProgress(QuestionProgressEntity(questionId = 1), isCorrect = true, now = now)
        assertEquals(1, next.correctStreak)
        assertEquals(2.6, next.easinessFactor, 1e-9)
        assertEquals(1, next.interval)
        assertEquals(now + day, next.nextReviewDate!!.time)
        assertEquals(now, next.lastReviewedDate!!.time)
    }

    @Test
    fun correctAnswerGrowsIntervalByEasiness() {
        val progress = QuestionProgressEntity(questionId = 1, correctStreak = 2, interval = 4, easinessFactor = 2.5)
        val next = SrsMath.nextProgress(progress, isCorrect = true, now = now)
        assertEquals(10, next.interval) // 4 × 2.5
        assertEquals(now + 10 * day, next.nextReviewDate!!.time)
    }

    @Test
    fun wrongAnswerResetsStreakAndFloorsEasiness() {
        val progress = QuestionProgressEntity(questionId = 1, correctStreak = 5, interval = 20, easinessFactor = 1.4)
        val next = SrsMath.nextProgress(progress, isCorrect = false, now = now)
        assertEquals(0, next.correctStreak)
        assertEquals(1.3, next.easinessFactor, 1e-9)
        assertEquals(1, next.interval)
    }

    @Test
    fun eloScoreNeverGoesNegative() {
        assertEquals(110.0, SrsMath.nextEloScore(100.0, isCorrect = true), 0.0)
        assertEquals(95.0, SrsMath.nextEloScore(100.0, isCorrect = false), 0.0)
        assertEquals(0.0, SrsMath.nextEloScore(3.0, isCorrect = false), 0.0)
    }
}
