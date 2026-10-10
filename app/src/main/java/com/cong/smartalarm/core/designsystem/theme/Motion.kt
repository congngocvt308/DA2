package com.cong.smartalarm.core.designsystem.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Thời gian bề mặt "lún" khi nhấn (soft press) — không nảy. */
const val SoftPressMillis = 150

/** Một nhịp thở: hít 4 giây, thở ra 4 giây. Dùng cho mọi hiệu ứng "thở". */
const val BreathCycleMillis = 8_000L

/** Người dùng đã tắt hiệu ứng chuyển động trong cài đặt hệ thống (Trợ năng). */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
