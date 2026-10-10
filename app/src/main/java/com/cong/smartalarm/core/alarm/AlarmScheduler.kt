package com.cong.smartalarm.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.cong.smartalarm.core.database.entity.AlarmEntity
import com.cong.smartalarm.feature.alarmedit.AlarmSettingData // Model của bạn
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.jvm.java

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Đặt lịch cho lần reo kế tiếp của [alarm], tính theo ngày lặp đã chọn (xem [NextAlarmTime]).
     * @param from mốc bắt đầu tìm; khi vừa reo xong, AlarmReceiver truyền mốc sau phút hiện tại
     * để không đặt lại đúng lần vừa reo.
     */
    fun schedule(alarm: AlarmEntity, from: LocalDateTime = LocalDateTime.now()) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // Gửi ID để Receiver xử lý
            putExtra("ALARM_ID", alarm.alarmId)
            // Gửi Label để Service hiển thị lên màn hình
            putExtra("ALARM_LABEL", alarm.label ?: "Báo thức")
            putExtra("RINGTONE_URI", alarm.ringtoneUri)
            // Nếu AlarmEntity ko có volume, hãy để mặc định hoặc thêm vào model
            putExtra("ALARM_VOLUME", 0.7f)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.alarmId, // Sử dụng alarmId làm requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Trước đây chỉ là "hôm nay hoặc ngày mai" nên báo thức lặp reo cả những ngày không chọn
        val triggerAtMillis = NextAlarmTime.after(from, alarm.hour, alarm.minute, alarm.daysOfWeek)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Sử dụng setAlarmClock là cách tốt nhất vì nó tự động đánh thức máy (Wakeup)
        // và cho phép hiện màn hình toàn màn hình
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
                    pendingIntent
                )
            } else {
                // Logic nhắc người dùng cấp quyền (như bạn đã viết)
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent),
                pendingIntent
            )
        }
    }
    fun cancel(alarm: AlarmEntity) {
        val pendingIntent = createPendingIntent(alarm)
        alarmManager.cancel(pendingIntent)
    }
    // Trong AlarmScheduler của bạn
    private fun createPendingIntent(alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.alarmId, // 🚨 ĐÂY LÀ KHÓA CHÍNH: RequestCode = alarmId
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}