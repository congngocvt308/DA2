package com.cong.smartalarm.feature.ringing

import com.cong.smartalarm.feature.quiz.QuizScreen

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cong.smartalarm.core.alarm.AlarmDismissHelper
import com.cong.smartalarm.core.alarm.AlarmReceiver
import com.cong.smartalarm.core.alarm.AlarmService
import com.cong.smartalarm.core.database.AppDatabase
import com.cong.smartalarm.navigation.Screen
import com.cong.smartalarm.feature.qr.QRDismissScreen
import com.cong.smartalarm.feature.smile.FaceMissionScreen
import com.cong.smartalarm.feature.smile.FaceMissionViewModel
import com.cong.smartalarm.feature.smile.FaceMissionViewModelFactory
import com.cong.smartalarm.feature.stats.StatsViewModel
import com.cong.smartalarm.feature.stats.StatsViewModelFactory
import com.cong.smartalarm.core.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import kotlin.jvm.java

class AlarmRingingActivity : ComponentActivity() {

    private var alarmId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguard()

        val label = intent.getStringExtra("ALARM_LABEL") ?: "Báo thức"
        alarmId = intent.getIntExtra("ALARM_ID", -1)
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
                        var isSnoozeEnabled by remember { mutableStateOf(true) }
                        
                        LaunchedEffect(alarmId) {
                            if (alarmId != -1) {
                                val alarm = appDao.getAlarmById(alarmId)
                                isSnoozeEnabled = alarm?.snoozeEnabled ?: true
                            }
                        }

                        AlarmRingingScreen(
                            alarmLabel = label,
                            hasQRCodes = hasQRCodes,
                            isSnoozeEnabled = isSnoozeEnabled,
                            onSnooze = {
                                lifecycleScope.launch {
                                    // 1. Dùng AppDao để lấy cấu hình báo thức (thời gian báo lại)
                                    val alarm = appDao.getAlarmById(alarmId)
                                    val snoozeDuration = alarm?.snoozeDuration ?: 5 // Mặc định 5p nếu không tìm thấy

                                    // 2. Dùng StatisticsDao để ghi nhận số lần snooze (phục vụ tính điểm tỉnh táo)
                                    statsDao.incrementSnoozeCount(alarmId)

                                    // 3. Gọi hàm đặt lịch reo lại với đúng thời gian đã lấy từ DB
                                    scheduleSnoozeAlarm(alarmId, label, snoozeDuration)

                                    stopRingingAndClose()
                                }
                            },
                            onNavigateToQuiz = {
                                lifecycleScope.launch {
                                    val faceConfig = appDao.getFaceMissionConfigByAlarmId(alarmId)
                                    val alarm = appDao.getAlarmById(alarmId)
                                    
                                    when {
                                        faceConfig != null -> {
                                            val bgUri = faceConfig.backgroundImageUri ?: ""
                                            navController.navigate("face_mission_screen?bgUri=$bgUri")
                                        }
                                        alarm != null && alarm.questionCount > 0 -> {
                                            navController.navigate("quiz_screen")
                                        }
                                        hasQRCodes -> {
                                            navController.navigate("qr_dismiss_screen")
                                        }
                                        else -> {
                                            statsViewModel.updatePerformanceAfterAlarm()
                                            navController.navigate("success_screen") {
                                                popUpTo(0)
                                            }
                                        }
                                    }
                                }
                            },
                            onNavigateToQRScan = {
                                navController.navigate("qr_dismiss_screen")
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
                                statsViewModel.updatePerformanceAfterAlarm()
                                navController.navigate("success_screen") {
                                    popUpTo(0)
                                }
                            }
                        )
                    }

                    // Mới: Màn hình quét mặt (Face Mission)
                    composable("face_mission_screen?bgUri={bgUri}") { backStackEntry ->
                        val bgUri = backStackEntry.arguments?.getString("bgUri")
                        val faceViewModel: FaceMissionViewModel = viewModel(
                            factory = FaceMissionViewModelFactory(context, alarmId, bgUri)
                        )
                        FaceMissionScreen(
                            viewModel = faceViewModel,
                            onLuongAComplete = {
                                statsViewModel.updatePerformanceAfterAlarm()
                                navController.navigate("success_screen") {
                                    popUpTo(0)
                                }
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
                                navController.navigate("success_screen") {
                                    popUpTo(0)
                                }
                            }
                        )
                    }

                    // Mới: Màn hình thành công rực rỡ
                    composable("success_screen") {
                        SuccessScreen(onFinished = { completeAlarmDismiss() })
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
            val snoozeIntent = Intent(this, com.cong.smartalarm.core.alarm.AlarmReceiver::class.java).apply {
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

    private fun completeAlarmDismiss() {
        lifecycleScope.launch {
            AlarmDismissHelper.onAlarmDismissed(this@AlarmRingingActivity, alarmId)
            stopRingingAndClose()
        }
    }

    private fun stopRingingAndClose() {
        AlarmDismissHelper.stopAlarmService(this)
        finishAndRemoveTask()
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