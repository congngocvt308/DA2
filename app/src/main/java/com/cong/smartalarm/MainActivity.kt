package com.cong.smartalarm

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cong.smartalarm.navigation.MainScreen
import com.cong.smartalarm.navigation.Screen
import com.cong.smartalarm.core.designsystem.theme.SmartAlarmTheme

/** Độ dài đồng hồ chuyển động trên splash (res/drawable-v31/splash_clock_anim.xml) + một chút nghỉ. */
private const val SplashIconMillis = 1_000L
private const val SplashFadeMillis = 150L

class MainActivity : ComponentActivity() {

    private var pendingNavigation = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash hệ thống phải cài trước super.onCreate. Android 12+: đồng hồ chuyển động ~0,9 giây
        // chạy trong lúc app tải; nếu app tải xong sớm hơn thì giữ thêm cho hết đoạn chuông reo.
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition {
            SystemClock.uptimeMillis() - Process.getStartUptimeMillis() < SplashIconMillis
        }
        splash.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .setDuration(SplashFadeMillis)
                .withEndAction { provider.remove() }
                .start()
        }
        // Nền đêm luôn tối -> icon thanh trạng thái/điều hướng luôn sáng
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        checkIntent(intent)

        setContent {
            SmartAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(initialExternalRoute = pendingNavigation.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        val destination = intent?.getStringExtra("NAVIGATE_TO")
        if (destination == "QUIZ_SCREEN") {
            pendingNavigation.value = Screen.QUIZ_SCREEN
        }
    }
}