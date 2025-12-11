package com.example.myapplication.alarm_logic
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//
//class AlarmReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        // Khi đến giờ -> Khởi động Service để phát nhạc
//        val serviceIntent = Intent(context, AlarmService::class.java).apply {
//            putExtra("ALARM_ID", intent.getIntExtra("ALARM_ID", -1))
//            putExtra("ALARM_LABEL", intent.getStringExtra("ALARM_LABEL"))
//        }
//        context.startForegroundService(serviceIntent)
//    }
//}