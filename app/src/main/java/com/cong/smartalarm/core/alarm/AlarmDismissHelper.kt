package com.cong.smartalarm.core.alarm

import android.content.Context
import android.content.Intent
import com.cong.smartalarm.core.di.appContainer

object AlarmDismissHelper {

    /**
     * Gọi khi người dùng tắt báo thức thành công (quiz, QR, v.v.).
     * Báo thức một lần (nhanh hoặc không lặp) sẽ bị xóa khỏi danh sách.
     * Báo thức lặp theo ngày vẫn giữ trong DB để reo các lần sau.
     */
    suspend fun onAlarmDismissed(context: Context, alarmId: Int) {
        if (alarmId <= 0) return
        val appContext = context.applicationContext
        val dao = appContext.appContainer.alarmRepository
        val scheduler = appContext.appContainer.alarmScheduler
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
