package com.cong.smartalarm.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cong.smartalarm.core.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        if (alarmId == -1) return

        // 🚨 QUAN TRỌNG: Sử dụng goAsync() để làm việc với Coroutine trong Receiver
        val pendingResult = goAsync()

        val db = AppDatabase.getDatabase(context)
        val alarmDao = db.appDao()
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Lấy dữ liệu báo thức từ Database
                val alarm = alarmDao.getAlarmById(alarmId)

                if (alarm != null) {
                    // 2. XỬ LÝ LOGIC VÒNG ĐỜI (Lặp lại hoặc Tắt)
                    if (alarm.daysOfWeek.isEmpty()) {
                        // Báo thức 1 lần -> Tắt Switch trên màn hình
                        alarmDao.updateAlarmEnabledStatus(alarm.alarmId, false)
                    } else {
                        // Báo thức lặp lại -> Đặt lịch cho ngày kế tiếp dựa trên daysOfWeek
                        scheduler.schedule(alarm)
                    }

                    // 3. KHỞI CHẠY SERVICE (Logic cũ của bạn)
                    val serviceIntent = Intent(context, AlarmService::class.java).apply {
                        // Truyền lại ID hoặc toàn bộ dữ liệu cần thiết
                        putExtra("ALARM_ID", alarm.alarmId)
                        putExtra("ALARM_LABEL", alarm.label)
                        putExtra("ALARM_URI", alarm.ringtoneUri)
                    }

                    // Chạy Foreground Service để phát nhạc và hiện Notification
                    context.startForegroundService(serviceIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 🚨 Bắt buộc gọi finish() để báo cho hệ thống là Receiver đã làm xong việc
                pendingResult.finish()
            }
        }
    }
}