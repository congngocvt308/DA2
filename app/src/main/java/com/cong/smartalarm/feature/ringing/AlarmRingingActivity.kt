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
import com.cong.smartalarm.core.di.appContainer
import com.cong.smartalarm.navigation.Screen
import com.cong.smartalarm.feature.qr.QRDismissScreen
import com.cong.smartalarm.feature.smile.FaceMissionScreen
import com.cong.smartalarm.feature.smile.FaceMissionViewModel
import com.cong.smartalarm.feature.stats.StatsViewModel
import com.cong.smartalarm.core.designsystem.theme.SmartAlarmTheme
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
            SmartAlarmTheme {
                // Khởi tạo NavController nội bộ
                val context = LocalContext.current
                val alarmRepository = remember { context.appContainer.alarmRepository }
                val statsRepository = remember { context.appContainer.statsRepository }

                // Khởi tạo ViewModel giống như ở màn hình Stats
                val statsViewModel: StatsViewModel = viewModel(
                    factory = StatsViewModel.Factory
                )

                val navController = rememberNavController()

                // Nhiệm vụ của báo thức: tên hiện trên màn reo, câu chúc trên màn hoàn thành
                var mission by remember { mutableStateOf(WakeMission.None) }
                var isSnoozeEnabled by remember { mutableStateOf(true) }
                LaunchedEffect(alarmId) {
                    if (alarmId != -1) {
                        val alarm = alarmRepository.getAlarmById(alarmId)
                        isSnoozeEnabled = alarm?.snoozeEnabled ?: true
                        mission = when {
                            alarmRepository.getFaceMissionConfigByAlarmId(alarmId) != null -> WakeMission.Smile
                            alarm != null && alarm.questionCount > 0 -> WakeMission.Quiz
                            hasQRCodes -> WakeMission.Qr
                            else -> WakeMission.None
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "ringing_screen"
                ) {
                    // 1. Màn hình báo thức đang reo
                    composable("ringing_screen") {
                        AlarmRingingScreen(
                            alarmLabel = label,
                            mission = mission,
                            isSnoozeEnabled = isSnoozeEnabled,
                            onSnooze = {
                                lifecycleScope.launch {
                                    // 1. Dùng AlarmRepository để lấy cấu hình báo thức (thời gian báo lại)
                                    val alarm = alarmRepository.getAlarmById(alarmId)
                                    val snoozeDuration = alarm?.snoozeDuration ?: 5 // Mặc định 5p nếu không tìm thấy

                                    // 2. Dùng StatsRepository để ghi nhận số lần snooze (phục vụ tính điểm tỉnh táo)
                                    statsRepository.incrementSnoozeCount(alarmId)

                                    // 3. Gọi hàm đặt lịch reo lại với đúng thời gian đã lấy từ DB
                                    scheduleSnoozeAlarm(alarmId, label, snoozeDuration)

                                    stopRingingAndClose()
                                }
                            },
                            onDismiss = {
                                lifecycleScope.launch {
                                    val faceConfig = alarmRepository.getFaceMissionConfigByAlarmId(alarmId)
                                    val alarm = alarmRepository.getAlarmById(alarmId)
                                    
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
                            factory = FaceMissionViewModel.factory(alarmId, bgUri)
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

                    // Màn hoàn thành: tắt chuông ngay khi vào, tự đóng sau vài giây (hoặc giữ sáng nếu người dùng chạm)
                    composable("success_screen") {
                        LaunchedEffect(Unit) { completeAlarmDismiss() }
                        SuccessScreen(mission = mission, onFinished = { finishAndRemoveTask() })
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
        AlarmDismissHelper.stopAlarmService(this)
        lifecycleScope.launch {
            AlarmDismissHelper.onAlarmDismissed(this@AlarmRingingActivity, alarmId)
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