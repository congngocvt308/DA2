package com.example.myapplication.alarm_logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.myapplication.data.AlarmEntity
import com.example.myapplication.data.AlarmSettingData // Model của bạn
import java.util.Calendar
import kotlin.jvm.java

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmEntity) {
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

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Sử dụng setAlarmClock là cách tốt nhất vì nó tự động đánh thức máy (Wakeup)
        // và cho phép hiện màn hình toàn màn hình
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                    pendingIntent
                )
            } else {
                // Logic nhắc người dùng cấp quyền (như bạn đã viết)
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                pendingIntent
            )
        }
    }
    fun cancel(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}