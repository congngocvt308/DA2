package com.cong.smartalarm.feature.stats

import com.cong.smartalarm.core.database.model.DailyStat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class WeeklyAccuracyTest {

    // Thứ Bảy 10/10/2026
    private val today = LocalDate.of(2026, 10, 10)

    @Test
    fun buildsSevenDaysOldestFirstEndingToday() {
        val days = WeeklyAccuracy.build(emptyList(), today)
        assertEquals(7, days.size)
        assertEquals(listOf("CN", "T2", "T3", "T4", "T5", "T6", "Nay"), days.map { it.label })
        assertEquals(listOf(4, 5, 6, 7, 8, 9, 10), days.map { it.dayOfMonth })
        assertTrue(days.last().isToday)
        assertFalse(days.first().isToday)
    }

    @Test
    fun dayWithoutAnswersIsNullNotZero() {
        val days = WeeklyAccuracy.build(listOf(DailyStat(day = "2026-10-09", correct = 0, total = 4)), today)
        assertEquals(0f, days[5].accuracy!!, 0f) // trả lời sai hết = 0%
        assertNull(days[4].accuracy)             // không luyện tập = chưa có dữ liệu
        assertEquals(0, days[4].answered)
    }

    @Test
    fun accuracyAndCountsComeFromMatchingDay() {
        val days = WeeklyAccuracy.build(listOf(DailyStat(day = "2026-10-10", correct = 3, total = 4)), today)
        assertEquals(0.75f, days.last().accuracy!!, 1e-6f)
        assertEquals(3, days.last().correct)
        assertEquals(4, days.last().answered)
    }

    @Test
    fun rowsOutsideWindowAreIgnored() {
        val days = WeeklyAccuracy.build(listOf(DailyStat(day = "2026-10-03", correct = 1, total = 1)), today)
        assertTrue(days.all { it.accuracy == null })
    }

    @Test
    fun windowStartsAtMidnightSixDaysAgo() {
        val start = WeeklyAccuracy.windowStartMillis(today, ZoneOffset.UTC)
        assertEquals(LocalDate.of(2026, 10, 4).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(), start)
    }
}
