package com.example.myapplication.alarm_logic

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
//import com.example.myapplication.ui.AlarmRingingActivity

//class AlarmService : Service() {
//
//    private var mediaPlayer: MediaPlayer? = null
//    private var vibrator: Vibrator? = null
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Báo thức"
//
//        // 1. Tạo Notification Channel (Bắt buộc cho Android 8+)
//        createNotificationChannel()
//
//        // 2. Intent để mở màn hình Rung chuông (Full Screen Intent)
//        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            // Truyền dữ liệu sang màn hình rung chuông
//            putExtra("ALARM_LABEL", alarmLabel)
//        }
//
//        val fullScreenPendingIntent = PendingIntent.getActivity(
//            this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // 3. Tạo thông báo (Notification)
//        val notification = NotificationCompat.Builder(this, "ALARM_CHANNEL_ID")
//            .setSmallIcon(R.drawable.ic_alarm) // Nhớ thêm icon vào drawable
//            .setContentTitle("Báo thức đang kêu!")
//            .setContentText(alarmLabel)
//            .setPriority(NotificationCompat.PRIORITY_MAX)
//            .setCategory(NotificationCompat.CATEGORY_ALARM)
//            .setFullScreenIntent(fullScreenPendingIntent, true) // QUAN TRỌNG: Để hiện đè lên màn hình khóa
//            .build()
//
//        // 4. Chạy Service dưới dạng Foreground
//        startForeground(1, notification)
//
//        // 5. Phát nhạc
//        startRinging()
//        startVibrate()
//
//        return START_STICKY
//    }
//
//    private fun startRinging() {
//        // Lấy nhạc chuông mặc định
//        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//        mediaPlayer = MediaPlayer.create(this, alarmUri)
//        mediaPlayer?.isLooping = true // Lặp lại
//        mediaPlayer?.start()
//    }
//
//    private fun startVibrate() {
//        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        // Rung: nghỉ 1s, rung 2s, lặp lại (0 là lặp vô tận)
//        vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mediaPlayer?.stop()
//        mediaPlayer?.release()
//        vibrator?.cancel()
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    private fun createNotificationChannel() {
//        val channel = NotificationChannel(
//            "ALARM_CHANNEL_ID",
//            "Kênh Báo Thức",
//            NotificationManager.IMPORTANCE_HIGH
//        )
//        channel.setSound(null, null) // Tắt tiếng của notification để tự quản lý bằng MediaPlayer
//        val manager = getSystemService(NotificationManager::class.java)
//        manager.createNotificationChannel(channel)
//    }
//}