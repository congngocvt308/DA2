package com.example.myapplication.alarm_logic
//
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import com.example.myapplication.data.AlarmItem // Model Alarm của bạn
//import java.util.Calendar
//
//class AlarmScheduler(private val context: Context) {
//
//    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//    fun schedule(alarm: AlarmItem) {
//        val intent = Intent(context, AlarmReceiver::class.java).apply {
//            putExtra("ALARM_ID", alarm.id)
//            putExtra("ALARM_LABEL", alarm.label)
//            // Truyền thêm thông tin nhiệm vụ nếu cần
//        }
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            alarm.id, // ID duy nhất để không bị trùng
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Tính toán thời gian báo thức
//        val calendar = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, alarm.hour)
//            set(Calendar.MINUTE, alarm.minute)
//            set(Calendar.SECOND, 0)
//        }
//
//        // Nếu giờ đặt đã qua so với hiện tại -> Đặt cho ngày mai
//        if (calendar.timeInMillis <= System.currentTimeMillis()) {
//            calendar.add(Calendar.DAY_OF_YEAR, 1)
//        }
//
//        // Đặt báo thức chính xác
//        try {
//            alarmManager.setAlarmClock(
//                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
//                pendingIntent
//            )
//        } catch (e: Exception) {
//            e.printStackTrace() // Xử lý trường hợp thiếu quyền
//        }
//    }
//
//    fun cancel(alarm: AlarmItem) {
//        val intent = Intent(context, AlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            alarm.id,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        alarmManager.cancel(pendingIntent)
//    }
//}