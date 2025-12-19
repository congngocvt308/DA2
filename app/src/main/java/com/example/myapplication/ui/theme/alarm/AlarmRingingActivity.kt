package com.example.myapplication.ui.theme.alarm

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.alarm_logic.AlarmService
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.MyApplicationTheme
import kotlin.jvm.java

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguard()

        val label = intent.getStringExtra("ALARM_LABEL") ?: "Báo thức"

        setContent {
            MyApplicationTheme {
                // Khởi tạo NavController nội bộ
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "ringing_screen"
                ) {
                    // 1. Màn hình báo thức đang reo
                    composable("ringing_screen") {
                        AlarmRingingScreen(
                            alarmLabel = label,
                            onSnooze = {
                                stopRinging()
                                // Logic đặt báo thức lại sau 5p (nếu có)
                                finish()
                            },
                            onNavigateToQuiz = {
                                // Chuyển sang màn hình Quiz nội bộ
                                navController.navigate("quiz_screen")
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
                            // viewModel tự động lấy từ QuizViewModel = viewModel()
                            // nên bạn không cần truyền thủ công nếu không muốn
                            onBack = {
                                // Khi ấn nút Back trong Quiz, quay lại màn hình reo
                                navController.popBackStack()
                            },
                            onQuizCompleted = {
                                // Khi giải xong: Tắt nhạc và đóng Activity
                                stopRinging()
                                finish()
                            }
                        )
                    }
                }
            }
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