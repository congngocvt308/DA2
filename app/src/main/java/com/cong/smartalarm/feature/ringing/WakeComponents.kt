package com.cong.smartalarm.feature.ringing

import android.app.Activity
import android.view.Window
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.cong.smartalarm.core.designsystem.theme.Wake
import java.util.WeakHashMap

/** Nhiệm vụ phải làm để tắt báo thức: tên hiện trên màn reo + câu chúc trên màn hoàn thành. */
enum class WakeMission(val title: String?, val cheer: String) {
    Smile("Thử thách nụ cười", "Nụ cười đẹp lắm. Chúc bạn một ngày thật vui."),
    Quiz("Trả lời câu hỏi", "Đầu óc tỉnh táo rồi đó. Chúc bạn một ngày thật vui."),
    Qr("Quét mã QR", "Bạn đã rời giường rồi. Chúc bạn một ngày thật vui."),
    /** Không có nhiệm vụ: màn reo hiện tên báo thức. */
    None(null, "Chúc bạn một ngày thật vui.")
}

/** Nền trời kem đào của màn reo / màn hoàn thành. */
fun Modifier.wakeSky(): Modifier = background(
    Brush.verticalGradient(0f to Wake.SkyTop, 0.55f to Wake.SkyMid, 1f to Wake.SkyBottom)
)

/** Màu thanh hệ thống trước khi màn sáng đầu tiên mở, kèm số màn sáng đang mở trên cửa sổ đó. */
private class SavedBars(val status: Int, val nav: Int, val lightStatus: Boolean, val lightNav: Boolean) {
    var users = 0
}

private val savedBars = WeakHashMap<Window, SavedBars>()

/**
 * Activity chứa màn reo còn có quiz / quét mặt nền tối, nên chỉ đổi thanh hệ thống sang sáng
 * khi màn này đang hiện và trả lại như cũ khi rời đi.
 * Đếm số màn sáng đang mở: khi màn reo chuyển sang màn hoàn thành, màn mới vào trước màn cũ rời đi,
 * nên chỉ trả màu cũ khi màn sáng cuối cùng đóng.
 */
@Suppress("DEPRECATION") // statusBarColor: Android 15+ tự vẽ sau thanh hệ thống, khi đó chỉ còn đổi màu icon
@Composable
fun LightSystemBars(top: Color = Wake.SkyTop, bottom: Color = Wake.SkyBottom) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val saved = savedBars.getOrPut(window) {
            SavedBars(
                window.statusBarColor,
                window.navigationBarColor,
                controller.isAppearanceLightStatusBars,
                controller.isAppearanceLightNavigationBars
            )
        }
        saved.users++
        window.statusBarColor = top.toArgb()
        window.navigationBarColor = bottom.toArgb()
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        onDispose {
            saved.users--
            if (saved.users == 0) {
                savedBars.remove(window)
                window.statusBarColor = saved.status
                window.navigationBarColor = saved.nav
                controller.isAppearanceLightStatusBars = saved.lightStatus
                controller.isAppearanceLightNavigationBars = saved.lightNav
            }
        }
    }
}

/** Nút viên thuốc dùng chung cho hai màn: nền đặc hoặc chỉ có viền. */
@Composable
fun WakePillButton(
    onClick: () -> Unit,
    height: Dp,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    inner: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        contentColor = content,
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(contentAlignment = Alignment.Center) { inner() }
    }
}

internal val WakeButtonOutline = BorderStroke(2.dp, Wake.Outline)
