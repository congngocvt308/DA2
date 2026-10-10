package com.cong.smartalarm.ui.theme.components

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import com.cong.smartalarm.ui.theme.theme.Calm
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/** Người dùng đã tắt hiệu ứng chuyển động trong cài đặt hệ thống (Trợ năng). */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/** Một nhịp thở: hít 4 giây, thở ra 4 giây. */
const val BreathCycleMillis = 8_000L

/**
 * Gốc giao diện của cả app: vẽ nền pastel yên tĩnh với hai quầng màu "thở" rất chậm
 * (nhịp 8 giây) và cung cấp [LocalGlassBackdrop] cho các bề mặt bên trong.
 * Chỉ cập nhật ~15 fps, đứng yên nếu người dùng bật giảm chuyển động.
 */
@Composable
fun GlassHost(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val breath = remember { mutableFloatStateOf(0.5f) }
    val reducedMotion = rememberReducedMotion()
    if (!reducedMotion) {
        LaunchedEffect(Unit) {
            val start = withFrameMillis { it }
            while (true) {
                withFrameMillis { t ->
                    val p = ((t - start) % BreathCycleMillis) / BreathCycleMillis.toFloat()
                    breath.floatValue = 0.5f - 0.5f * kotlin.math.cos(p * 2 * PI).toFloat()
                }
                delay(66)
            }
        }
    }
    val backdrop = rememberGlassBackdrop { canvas -> drawCalmBackground(canvas, breath.floatValue) }

    CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
        Box(modifier.fillMaxSize().glassBackdropHost(backdrop)) { content() }
    }
}

/**
 * Nền đơn sắc dịu: xám rất nhạt chuyển dần xuống, một quầng xám phía trên
 * và một quầng xanh lá wellness phía dưới, nở ra / thu lại theo [breath] (0..1).
 */
fun DrawScope.drawCalmBackground(canvas: Size, breath: Float) {
    val w = canvas.width
    val h = canvas.height
    if (w <= 0f || h <= 0f) return

    drawRect(
        Brush.verticalGradient(
            0f to Calm.Background,
            0.6f to Color(0xFFF7F7F8),
            1f to Color(0xFFF3F6FB),
            endY = h
        ),
        size = canvas
    )

    fun glow(color: Color, cx: Float, cy: Float, radius: Float) {
        drawCircle(
            Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center = Offset(cx, cy), radius = radius),
            radius = radius,
            center = Offset(cx, cy)
        )
    }
    val scale = 0.9f + 0.1f * breath
    glow(Calm.LavenderSoft.copy(alpha = 0.30f + 0.10f * breath), w * 0.12f, h * 0.10f, w * 0.85f * scale)
    glow(Calm.Wellness.copy(alpha = 0.07f + 0.04f * breath), w * 0.95f, h * 0.88f, w * 0.75f * scale)
    glow(Color(0xFFE4E4E7).copy(alpha = 0.35f), w * 0.95f, h * (0.32f + 0.01f * sin(breath * PI.toFloat())), w * 0.5f)
}
