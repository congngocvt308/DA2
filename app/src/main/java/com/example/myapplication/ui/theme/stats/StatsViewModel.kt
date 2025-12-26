package com.example.myapplication.ui.theme.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.QuestionEntity
import com.example.myapplication.data.StatisticsDao
import com.example.myapplication.data.UserStatsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.emptyList

class StatsViewModel(private val statsDao: StatisticsDao) : ViewModel() {

    init {
        viewModelScope.launch {
            // Khởi tạo user mặc định nếu chưa tồn tại
            statsDao.initializeUserStats(UserStatsEntity(userId = 1))
        }
    }

    // 1. Dữ liệu thô từ DAO (Tự động cập nhật nhờ Flow)
    // Trong StatsViewModel.kt
    val weeklyAccuracy = statsDao.getWeeklyAccuracy(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
        .map { list ->
            val last7Days = mutableListOf<Pair<String, Float>>()
            val calendar = Calendar.getInstance()

            // Tạo danh sách 7 ngày gần nhất
            for (i in 6 downTo 0) {
                val date = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -i)
                }
                val dateStr = SimpleDateFormat("MM-dd", Locale.getDefault()).format(date.time)

                // Tìm dữ liệu trong list từ DAO, nếu không thấy thì để 0f
                val foundData = list.find { it.day.endsWith(dateStr) }
                val accuracy = if (foundData != null) foundData.correct.toFloat() / foundData.total else 0f

                // Format nhãn: Nếu là hôm nay thì ghi "Nay", còn lại ghi Thứ hoặc Ngày
                val label = if (i == 0) "Nay" else dateStr.takeLast(2) // Chỉ lấy phần Ngày cho gọn
                last7Days.add(label to accuracy)
            }
            last7Days
        }
    val srsDistribution = statsDao.getSrsDistribution()

    // 2. Logic tính Wake-up Score
    private val _wakeUpScore = MutableStateFlow(0f)
    val wakeUpScore = _wakeUpScore.asStateFlow()

    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus = _selectedStatus.asStateFlow()

    // 🚨 ĐÃ FIX: Chỉ định rõ <QuestionEntity> và gọi qua statsDao
    val filteredQuestions = _selectedStatus.flatMapLatest { status ->
        if (status == null) {
            flowOf(emptyList<com.example.myapplication.data.QuestionEntity>())
        } else {
            // Gọi hàm từ statsDao
            statsDao.getQuestionsBySrsStatus(status)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val userStats = statsDao.getUserStats()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun selectSrsStatus(status: String?) {
        _selectedStatus.value = status
    }

    fun calculateWakeUpPerformance() {
        viewModelScope.launch {
            val histories = statsDao.getRecentAlarmHistory()
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
            val currentStats = statsDao.getUserStatsSynchronous() ?: UserStatsEntity(userId = 1)
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
            statsDao.updateUserStats(updatedStats)
        }
    }

    private fun checkIsYesterday(lastActiveTimestamp: Long): Boolean {
        val calLast = Calendar.getInstance().apply { timeInMillis = lastActiveTimestamp }
        val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        return calLast.get(Calendar.YEAR) == calYesterday.get(Calendar.YEAR) &&
                calLast.get(Calendar.DAY_OF_YEAR) == calYesterday.get(Calendar.DAY_OF_YEAR)
    }
}

class StatsViewModelFactory(private val statsDao: StatisticsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(statsDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}