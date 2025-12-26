package com.example.myapplication.ui.theme.alarm

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.alarm_logic.AlarmReceiver
import com.example.myapplication.alarm_logic.AlarmService
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.qrcode.QRDismissScreen
import com.example.myapplication.ui.theme.stats.StatsViewModel
import com.example.myapplication.ui.theme.stats.StatsViewModelFactory
import com.example.myapplication.ui.theme.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlin.jvm.java

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguard()

        val label = intent.getStringExtra("ALARM_LABEL") ?: "Báo thức"
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val hasQRCodes = intent.getBooleanExtra("HAS_QR_CODES", false)

        setContent {
            MyApplicationTheme {
                // Khởi tạo NavController nội bộ
                val context = LocalContext.current
                val database = AppDatabase.getDatabase(context.applicationContext)
                val statsDao = database.statisticsDao()
                val appDao = database.appDao()

                // Khởi tạo ViewModel giống như ở màn hình Stats
                val statsViewModel: StatsViewModel = viewModel(
                    factory = StatsViewModelFactory(statsDao)
                )

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "ringing_screen"
                ) {
                    // 1. Màn hình báo thức đang reo
                    composable("ringing_screen") {
                        AlarmRingingScreen(
                            alarmLabel = label,
                            hasQRCodes = hasQRCodes,
                            onSnooze = {
                                lifecycleScope.launch {
                                    // 1. Dùng AppDao để lấy cấu hình báo thức (thời gian báo lại)
                                    val alarm = appDao.getAlarmById(alarmId)
                                    val snoozeDuration = alarm?.snoozeDuration ?: 5 // Mặc định 5p nếu không tìm thấy

                                    // 2. Dùng StatisticsDao để ghi nhận số lần snooze (phục vụ tính điểm tỉnh táo)
                                    statsDao.incrementSnoozeCount(alarmId)

                                    // 3. Gọi hàm đặt lịch reo lại với đúng thời gian đã lấy từ DB
                                    scheduleSnoozeAlarm(alarmId, label, snoozeDuration)

                                    stopRinging()
                                    finish()
                                }
                            },
                            onNavigateToQuiz = {
                                // Chuyển sang màn hình Quiz nội bộ
                                navController.navigate("quiz_screen")
                            },
                            onNavigateToQRScan = {
                                // Chuyển sang màn hình quét QR
                                navController.navigate("qr_dismiss_screen")
                            },
                            onFinish = {
                                // Tắt hẳn (nếu người dùng không chọn quiz - tùy logic của bạn)
                                stopRinging()
                                finish()
                            }
                        )
                    }

                    // 2. Màn hình giải đố (Quiz) - Lắp đúng tham số của bạn vào đây
                    composable("quiz_screen") {
                        QuizScreen(
                            alarmId = alarmId,
                            onBack = {
                                // Khi ấn nút Back trong Quiz, quay lại màn hình reo
                                navController.popBackStack()
                            },
                            onQuizCompleted = {
                                // Khi giải xong: Tắt nhạc và đóng Activity
                                statsViewModel.updatePerformanceAfterAlarm()
                                stopRinging()
                                finish()
                            }
                        )
                    }
                    
                    // 3. Màn hình quét QR/Barcode để tắt báo thức
                    composable("qr_dismiss_screen") {
                        QRDismissScreen(
                            alarmId = alarmId,
                            onBack = {
                                navController.popBackStack()
                            },
                            onDismissSuccess = {
                                statsViewModel.updatePerformanceAfterAlarm()
                                stopRinging()
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    // Trong AlarmRingingActivity.kt
    private fun scheduleSnoozeAlarm(alarmId: Int, label: String, durationInMinutes: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Kiểm tra quyền Android 12+ như bước trước
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
            return
        }

        try {
            val snoozeIntent = Intent(this, com.example.myapplication.alarm_logic.AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", label)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this, alarmId, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 🚨 THAY ĐỔI TẠI ĐÂY: Tính toán dựa trên durationInMinutes từ Database
            val snoozeMillis = durationInMinutes * 60 * 1000L
            val triggerTime = System.currentTimeMillis() + snoozeMillis

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            android.util.Log.e("AlarmError", "Lỗi: ${e.message}")
        }
    }

    private fun stopRinging() {
        val intent = Intent(this, AlarmService::class.java)
        stopService(intent)
    }

    // Cấu hình quan trọng để sáng màn hình khi điện thoại đang khóa
    private fun turnScreenOnAndKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        }
    }
}