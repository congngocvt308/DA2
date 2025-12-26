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
import com.example.myapplication.ui.theme.MainActivity
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
        val ringtoneUri = intent?.getStringExtra("ALARM_URI") ?: ""
        val volume = intent?.getFloatExtra("ALARM_VOLUME", 0.7f) ?: 0.7f
        val label = intent?.getStringExtra("ALARM_LABEL") ?: "Báo thức"

        // Kiểm tra xem báo thức có QR codes không
        serviceScope.launch {
            val dao = AppDatabase.getDatabase(applicationContext).appDao()
            val qrCodeCount = if (alarmId > 0) {
                dao.getQRLinkCountForAlarm(alarmId)
            } else 0
            val hasQRCodes = qrCodeCount > 0

            val notification = createNotification(label, alarmId, hasQRCodes)
            startForeground(1, notification)
            
            val activityIntent = Intent(this@AlarmService, AlarmRingingActivity::class.java).apply {
                // Flag quan trọng để tách khỏi MainActivity
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("ALARM_LABEL", label)
                putExtra("ALARM_ID", alarmId)
                putExtra("HAS_QR_CODES", hasQRCodes)
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
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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

    private fun createNotification(label: String, alarmId: Int, hasQRCodes: Boolean): Notification {
        val channelId = "ALARM_CHANNEL"

        // 🚨 ĐỔI MainActivity THÀNH AlarmRingingActivity
        val ringingIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_ID", alarmId)
            putExtra("HAS_QR_CODES", hasQRCodes)
            // Flag quan trọng để nó hiện lên ngay cả khi đang khóa
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alarmId, // Dùng ID để tránh chồng lấn
            ringingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Báo thức: $label")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            // 🚨 Dòng này sẽ bật AlarmRingingActivity lên màn hình khóa
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}