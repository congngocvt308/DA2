package com.cong.smartalarm.core.alarm

import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Nguồn duy nhất cho câu hỏi "báo thức reo lần tới lúc nào?" — dùng chung cho AlarmScheduler
 * (đặt lịch thật với hệ thống) và các màn hình (dòng "Đổ chuông sau…"), để hai bên không lệch nhau.
 *
 * Ngày lặp lưu dạng mã "T2".."T7", "CN". Tập rỗng = báo thức một lần.
 */
object NextAlarmTime {

    private val dayCodes = mapOf(
        DayOfWeek.MONDAY to "T2",
        DayOfWeek.TUESDAY to "T3",
        DayOfWeek.WEDNESDAY to "T4",
        DayOfWeek.THURSDAY to "T5",
        DayOfWeek.FRIDAY to "T6",
        DayOfWeek.SATURDAY to "T7",
        DayOfWeek.SUNDAY to "CN"
    )

    fun dayCode(day: DayOfWeek): String = dayCodes.getValue(day)

    /**
     * Thời điểm reo kế tiếp, luôn **sau** [now].
     * - Một lần: hôm nay nếu giờ chưa qua, không thì ngày mai.
     * - Lặp: ngày gần nhất nằm trong [daysOfWeek] mà giờ chưa qua (có thể là cùng thứ tuần sau).
     */
    fun after(now: LocalDateTime, hour: Int, minute: Int, daysOfWeek: Set<String>): LocalDateTime {
        val todayAt = now.toLocalDate().atTime(hour, minute)
        val repeatDays = daysOfWeek.filter { it in dayCodes.values }.toSet()
        if (repeatDays.isEmpty()) {
            return if (todayAt.isAfter(now)) todayAt else todayAt.plusDays(1)
        }
        // 0..7: ngày 7 là cùng thứ của tuần sau, khi chỉ chọn hôm nay mà giờ đã qua
        return (0L..7L)
            .map { todayAt.plusDays(it) }
            .first { dayCode(it.dayOfWeek) in repeatDays && it.isAfter(now) }
    }
}
