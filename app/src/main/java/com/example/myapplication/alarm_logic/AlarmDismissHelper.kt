package com.example.myapplication.alarm_logic

import android.content.Context
import android.content.Intent
import com.example.myapplication.data.AppDatabase

object AlarmDismissHelper {

    /**
     * Gọi khi người dùng tắt báo thức thành công (quiz, QR, v.v.).
     * Báo thức một lần (nhanh hoặc không lặp) sẽ bị xóa khỏi danh sách.
     * Báo thức lặp theo ngày vẫn giữ trong DB để reo các lần sau.
     */
    suspend fun onAlarmDismissed(context: Context, alarmId: Int) {
        if (alarmId <= 0) return
        val appContext = context.applicationContext
        val dao = AppDatabase.getDatabase(appContext).appDao()
        val scheduler = AlarmScheduler(appContext)
        val alarm = dao.getAlarmById(alarmId) ?: return

        // Chỉ báo thức một lần (nhanh / không lặp): hủy lịch + xóa khỏi danh sách.
        // Báo thức lặp: lịch ngày kế đã được đặt khi reo — không cancel ở đây.
        if (alarm.daysOfWeek.isEmpty()) {
            scheduler.cancel(alarm)
            dao.deleteAlarm(alarm)
        }
    }

    fun stopAlarmService(context: Context) {
        context.stopService(Intent(context, AlarmService::class.java))
    }
}
