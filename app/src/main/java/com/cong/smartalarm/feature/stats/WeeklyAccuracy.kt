package com.cong.smartalarm.feature.stats

import com.cong.smartalarm.core.database.model.DailyStat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/** Một cột của biểu đồ 7 ngày. [accuracy] = null khi hôm đó chưa trả lời câu nào. */
data class DayAccuracy(
    val label: String,
    val dayOfMonth: Int,
    val accuracy: Float?,
    val correct: Int,
    val answered: Int,
    val isToday: Boolean
)

/** Dựng dữ liệu biểu đồ "Hiệu suất 7 ngày" — hàm thuần, không phụ thuộc giờ hệ thống nên kiểm thử được. */
object WeeklyAccuracy {
    const val DAYS = 7

    private val weekdayLabel = mapOf(
        DayOfWeek.MONDAY to "T2", DayOfWeek.TUESDAY to "T3", DayOfWeek.WEDNESDAY to "T4",
        DayOfWeek.THURSDAY to "T5", DayOfWeek.FRIDAY to "T6", DayOfWeek.SATURDAY to "T7", DayOfWeek.SUNDAY to "CN"
    )

    /** Mốc bắt đầu truy vấn: 0h của ngày cũ nhất trong cửa sổ (6 ngày trước [today]). */
    fun windowStartMillis(today: LocalDate, zone: ZoneId): Long =
        today.minusDays((DAYS - 1).toLong()).atStartOfDay(zone).toInstant().toEpochMilli()

    /**
     * 7 cột từ cũ đến mới, cột cuối là [today] ("Nay"). [stats] có `day` dạng yyyy-MM-dd
     * (định dạng date() của SQLite, cũng là LocalDate.toString()).
     */
    fun build(stats: List<DailyStat>, today: LocalDate): List<DayAccuracy> {
        val byDay = stats.associateBy { it.day }
        return (DAYS - 1 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val found = byDay[date.toString()]
            DayAccuracy(
                label = if (daysAgo == 0) "Nay" else weekdayLabel.getValue(date.dayOfWeek),
                dayOfMonth = date.dayOfMonth,
                // null = hôm đó chưa trả lời câu nào (khác với 0% = trả lời sai hết)
                accuracy = found?.takeIf { it.total > 0 }?.let { it.correct.toFloat() / it.total },
                correct = found?.correct ?: 0,
                answered = found?.total ?: 0,
                isToday = daysAgo == 0
            )
        }
    }
}
