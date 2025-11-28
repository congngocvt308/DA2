package com.example.myapplication.ui.theme.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AlarmData
import com.example.myapplication.data.AlarmEntity
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// 🚨 1. Sửa ViewModel để kế thừa AndroidViewModel (cần Context để mở DB)
class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    // 2. Khởi tạo DAO
    private val alarmDao = AppDatabase.getDatabase(application).appDao()

    // 3. Luồng dữ liệu: Lấy từ DB -> Chuyển đổi sang UI Model -> Đẩy lên UI
    val alarms: StateFlow<List<AlarmData>> = alarmDao.getAllAlarms()
        .map { entities ->
            entities.map { it.toAlarmData() } // Gọi hàm chuyển đổi (Mapping)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Giá trị ban đầu khi chưa load xong
        )

    // 4. Hàm thêm báo thức nhanh (Ghi vào DB)
    fun addQuickAlarm(minutesToAdd: Int) {
        viewModelScope.launch {
            val now = LocalTime.now().plusMinutes(minutesToAdd.toLong())

            // Tạo Entity để lưu vào DB
            val newEntity = AlarmEntity(
                hour = now.hour,
                minute = now.minute,
                label = "Báo thức nhanh",
                daysOfWeek = emptySet(), // Không lặp lại
                questionCount = 3,       // Mặc định 3 câu hỏi
                isEnabled = true
            )

            // Insert vào Room
            alarmDao.insertAlarm(newEntity)
        }
    }

    // 5. Hàm Bật/Tắt (Cập nhật DB)
    fun toggleAlarm(alarmId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            // Lấy báo thức gốc từ DB
            val oldAlarm = alarmDao.getAlarmById(alarmId)

            if (oldAlarm != null) {
                // Tạo bản sao với trạng thái mới
                val updatedAlarm = oldAlarm.copy(isEnabled = isEnabled)
                // Lưu ngược lại vào DB
                alarmDao.updateAlarm(updatedAlarm)
            }
        }
    }
}

// --- HÀM TIỆN ÍCH: CHUYỂN ĐỔI DATA (MAPPING) ---
// Chuyển từ Entity (trong DB) -> Data (hiển thị UI)
fun AlarmEntity.toAlarmData(): AlarmData {
    // 1. Format giờ phút: 6:5 -> "06:05"
    val timeString = String.format("%02d:%02d", this.hour, this.minute)

    // 2. Format ngày lặp lại: Set -> String hiển thị
    val daysString = if (this.daysOfWeek.isEmpty()) "Một lần"
    else this.daysOfWeek.sorted().joinToString(", ")

    return AlarmData(
        id = this.alarmId,
        time = timeString,
        days = daysString,
        label = this.label,
        isEnabled = this.isEnabled
    )
}