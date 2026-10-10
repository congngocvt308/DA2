package com.cong.smartalarm

import android.content.Intent
import android.os.Bundle
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

class MainActivity : ComponentActivity() {

    private var pendingNavigation = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Nền đêm luôn tối -> icon thanh trạng thái/điều hướng luôn sáng
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        installSplashScreen()

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