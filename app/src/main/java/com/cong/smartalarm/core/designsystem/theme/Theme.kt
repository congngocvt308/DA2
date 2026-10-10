package com.cong.smartalarm.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.cong.smartalarm.core.designsystem.component.CalmBackground

/** Theme gốc của app: màu Calm, chữ Lora/Raleway, bầu trời đêm "thở" phía sau mọi màn hình. Luôn tối. */
@Composable
fun SmartAlarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CalmColorScheme, typography = CalmTypography) {
        CalmBackground(content = content)
    }
}
