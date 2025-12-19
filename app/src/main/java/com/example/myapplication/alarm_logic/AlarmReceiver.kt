package com.example.myapplication.alarm_logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Nhận tín hiệu từ hệ thống -> Chuyển tiếp sang Service phát nhạc
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            // Copy toàn bộ dữ liệu (URI, Volume...) sang Service
            putExtras(intent)
        }

        // Bắt buộc dùng startForegroundService cho báo thức
        context.startForegroundService(serviceIntent)
    }
}