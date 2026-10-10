package com.cong.smartalarm.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cong.smartalarm.SmartAlarmApp
import com.cong.smartalarm.data.repository.StatsRepository
import com.cong.smartalarm.core.database.model.DailyStat
import com.cong.smartalarm.core.database.entity.QuestionEntity
import com.cong.smartalarm.core.database.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.emptyList

/** Một cột của biểu đồ 7 ngày. [accuracy] = null khi hôm đó chưa trả lời câu nào. */
data class DayAccuracy(
    val label: String,
    val dayOfMonth: Int,
    val accuracy: Float?,
    val correct: Int,
    val answered: Int,
    val isToday: Boolean
)

class StatsViewModel(private val statsRepository: StatsRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            // Khởi tạo user mặc định nếu chưa tồn tại
            statsRepository.initializeUserStats(UserStatsEntity(userId = 1))
        }
    }

    // 1. Dữ liệu thô từ DAO (Tự động cập nhật nhờ Flow)
    // Mốc "7 ngày" tính từ 0h của 6 ngày trước và được tính lại mỗi lần màn hình bắt đầu thu thập
    // (trước đây cố định lúc tạo ViewModel nên cột ngày cũ nhất bị mất buổi sáng và không sang ngày mới).
    val weeklyAccuracy: Flow<List<DayAccuracy>> = flow { emit(startOfDay(daysAgo = 6)) }
        .flatMapLatest { since ->
            statsRepository.getWeeklyAccuracy(since - 1).map { list -> buildLast7Days(list) }
        }

    private fun buildLast7Days(list: List<DailyStat>): List<DayAccuracy> {
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US) // trùng định dạng date() của SQLite
        val weekday = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        return (6 downTo 0).map { i ->
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val found = list.find { it.day == keyFormat.format(date.time) }
            DayAccuracy(
                label = if (i == 0) "Nay" else weekday[date.get(Calendar.DAY_OF_WEEK) - 1],
                dayOfMonth = date.get(Calendar.DAY_OF_MONTH),
                // null = hôm đó chưa trả lời câu nào (khác với 0% = trả lời sai hết)
                accuracy = found?.takeIf { it.total > 0 }?.let { it.correct.toFloat() / it.total },
                correct = found?.correct ?: 0,
                answered = found?.total ?: 0,
                isToday = i == 0
            )
        }
    }

    private fun startOfDay(daysAgo: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -daysAgo)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val srsDistribution = statsRepository.getSrsDistribution()

    // 2. Logic tính Wake-up Score
    private val _wakeUpScore = MutableStateFlow(0f)
    val wakeUpScore = _wakeUpScore.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()

    // 🚨 ĐÃ FIX: Chỉ định rõ <QuestionEntity> và gọi qua statsRepository
    val filteredQuestions = _selectedStatus.flatMapLatest { status ->
        if (status == null) {
            flowOf(emptyList<com.cong.smartalarm.core.database.entity.QuestionEntity>())
        } else {
            // Gọi hàm từ statsRepository
            statsRepository.getQuestionsBySrsStatus(status)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val userStats = statsRepository.getUserStats()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun selectSrsStatus(status: String?) {
        _selectedStatus.value = status
    }

    fun calculateWakeUpPerformance() {
        viewModelScope.launch {
            val histories = statsRepository.getRecentAlarmHistory()
            if (histories.isNotEmpty()) {
                val avg = histories.map { h ->
                    val delayMin = ((h.dismissalTime?.time ?: 0L) - h.firstRingTime.time) / 60000f
                    100f - (h.snoozeCount * 10f) - (delayMin * 0.5f)
                }.average().toFloat()
                _wakeUpScore.value = avg.coerceIn(0f, 100f)
            }
        }
    }

    fun updatePerformanceAfterAlarm() {
        viewModelScope.launch {
            val currentStats = statsRepository.getUserStatsSynchronous() ?: UserStatsEntity(userId = 1)
            val today = System.currentTimeMillis()

            // Giả sử mỗi lần hoàn thành báo thức được 10 điểm
            val newPoints = currentStats.totalPoints + 10

            // Logic tính streak đơn giản (so sánh ngày)
            val isConsecutive = checkIsYesterday(currentStats.lastActiveDate)
            val newStreak = if (isConsecutive) currentStats.currentStreak + 1 else 1

            val updatedStats = currentStats.copy(
                totalPoints = newPoints,
                currentStreak = newStreak,
                lastActiveDate = today,
                totalAlarmsDismissed = currentStats.totalAlarmsDismissed + 1
            )
            statsRepository.updateUserStats(updatedStats)
        }
    }

    private fun checkIsYesterday(lastActiveTimestamp: Long): Boolean {
        val calLast = Calendar.getInstance().apply { timeInMillis = lastActiveTimestamp }
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return calLast.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
                calLast.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartAlarmApp
                StatsViewModel(app.container.statsRepository)
            }
        }
    }
}
