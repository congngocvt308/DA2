package com.cong.smartalarm.core.alarm

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class NextAlarmTimeTest {

    // Thứ Năm 08/10/2026, 20:00
    private val thursdayEvening = LocalDateTime.of(2026, 10, 8, 20, 0)

    private fun next(now: LocalDateTime, days: Set<String>, hour: Int = 6, minute: Int = 30) =
        NextAlarmTime.after(now, hour, minute, days)

    @Test
    fun repeatingAlarmSkipsDaysNotSelected() {
        // Lỗi cũ: đặt T2 + T4 vào tối thứ Năm thì sáng thứ Sáu vẫn reo.
        val ring = next(thursdayEvening, setOf("T2", "T4"))
        assertEquals(LocalDateTime.of(2026, 10, 12, 6, 30), ring) // thứ Hai
    }

    @Test
    fun afterRingingMovesToNextSelectedDay() {
        // Vừa reo thứ Hai 6:30 (AlarmReceiver tìm từ sau phút hiện tại) -> thứ Tư, không phải thứ Ba
        val justRang = LocalDateTime.of(2026, 10, 12, 6, 31)
        assertEquals(LocalDateTime.of(2026, 10, 14, 6, 30), next(justRang, setOf("T2", "T4")))
    }

    @Test
    fun ringsLaterTodayWhenTodayIsSelectedAndTimeNotPassed() {
        val morning = LocalDateTime.of(2026, 10, 8, 5, 0) // thứ Năm 5:00
        assertEquals(LocalDateTime.of(2026, 10, 8, 6, 30), next(morning, setOf("T5")))
    }

    @Test
    fun onlyTodaySelectedButPassedRollsToNextWeek() {
        assertEquals(LocalDateTime.of(2026, 10, 15, 6, 30), next(thursdayEvening, setOf("T5")))
    }

    @Test
    fun sundayCodeIsCN() {
        assertEquals(LocalDateTime.of(2026, 10, 11, 6, 30), next(thursdayEvening, setOf("CN")))
    }

    @Test
    fun everyDayRingsTomorrowMorning() {
        val all = setOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        assertEquals(LocalDateTime.of(2026, 10, 9, 6, 30), next(thursdayEvening, all))
    }

    @Test
    fun oneTimeAlarmIsTodayOrTomorrow() {
        assertEquals(LocalDateTime.of(2026, 10, 9, 6, 30), next(thursdayEvening, emptySet()))
        assertEquals(LocalDateTime.of(2026, 10, 8, 21, 0), next(thursdayEvening, emptySet(), hour = 21, minute = 0))
    }

    @Test
    fun exactCurrentMinuteIsNotReturned() {
        // Luôn sau "now": cùng phút thì sang lần kế tiếp, tránh reo lại ngay
        assertEquals(LocalDateTime.of(2026, 10, 9, 20, 0), next(thursdayEvening, emptySet(), hour = 20, minute = 0))
    }

    @Test
    fun unknownDayCodesFallBackToOneTime() {
        assertEquals(LocalDateTime.of(2026, 10, 9, 6, 30), next(thursdayEvening, setOf("XX")))
    }
}
