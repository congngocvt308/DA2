package com.example.myapplication.alarm_logic

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.ui.theme.alarm.AlarmRingingActivity
import com.example.myapplication.ui.theme.alarm.AlarmRingingScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Lấy dữ liệu
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1
        val ringtoneUri = intent?.getStringExtra("RINGTONE_URI") ?: ""
        val volume = intent?.getFloatExtra("ALARM_VOLUME", 0.7f) ?: 0.7f
        val label = intent?.getStringExtra("ALARM_LABEL") ?: "Báo thức"

        val notification = createNotification(label)
        startForeground(1, notification)

        // Kiểm tra xem báo thức có QR codes không
        serviceScope.launch {
            val dao = AppDatabase.getDatabase(applicationContext).appDao()
            val qrCodeCount = if (alarmId > 0) {
                dao.getQRLinkCountForAlarm(alarmId)
            } else 0
            
            val activityIntent = Intent(this@AlarmService, AlarmRingingActivity::class.java).apply {
                // Flag quan trọng để tách khỏi MainActivity
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("ALARM_LABEL", label)
                putExtra("ALARM_ID", alarmId)
                putExtra("HAS_QR_CODES", qrCodeCount > 0)
            }
            startActivity(activityIntent)
        }

        playAlarmSound(ringtoneUri, volume)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "ALARM_CHANNEL"
            val channelName = "Báo thức hệ thống"

            // 🚨 QUAN TRỌNG: Phải để IMPORTANCE_HIGH để màn hình có thể tự bật lên
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Kênh dùng để hiển thị màn hình báo thức khi đang khóa"

                // Tắt tiếng mặc định của Notification vì bạn đã dùng MediaPlayer phát riêng
                setSound(null, null)

                // Cho phép hiển thị trên màn hình khóa
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC

                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun playAlarmSound(uriString: String, volume: Float) {
        try {
            val uri = if (uriString.isNotBlank()) Uri.parse(uriString) else null

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri ?: android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setVolume(volume, volume)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Trong AlarmService.kt, sửa hàm createNotification

    private fun createNotification(label: String): Notification {
        val channelId = "ALARM_CHANNEL"

        val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            // KHÔNG gửi sang MainActivity nữa
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            // Sử dụng FLAG_MUTABLE nếu bạn cần update Intent dữ liệu sau này
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Báo thức: $label")
            .setContentText("Vuốt để tắt hoặc chạm để mở")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Hiển thị nội dung trên lockscreen
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}