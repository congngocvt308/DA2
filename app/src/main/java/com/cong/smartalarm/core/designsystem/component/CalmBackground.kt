package com.cong.smartalarm.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.cong.smartalarm.core.designsystem.theme.BreathCycleMillis
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.core.designsystem.theme.rememberReducedMotion
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos

/**
 * Nền gốc của cả app: xám rất nhạt với hai quầng sáng "thở" chậm (nhịp [BreathCycleMillis]).
 * - Chỉ chạy khi app đang ở RESUMED (tắt màn hình / ra nền là dừng, đỡ pin).
 * - Cập nhật ~15 fps và chỉ đọc giá trị ở pha vẽ (drawBehind) nên không gây recompose.
 * - Đứng yên khi người dùng bật giảm chuyển động.
 */
@Composable
fun CalmBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val breath = remember { mutableFloatStateOf(0.5f) }
    val reducedMotion = rememberReducedMotion()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    if (!reducedMotion) {
        LaunchedEffect(lifecycle) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val start = withFrameMillis { it }
                while (true) {
                    withFrameMillis { t ->
                        val p = ((t - start) % BreathCycleMillis) / BreathCycleMillis.toFloat()
                        breath.floatValue = 0.5f - 0.5f * cos(p * 2 * PI).toFloat()
                    }
                    delay(66)
                }
            }
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .drawBehind { drawCalmBackground(size, breath.floatValue) }
    ) { content() }
}

/** Nền: xám rất nhạt chuyển dần xuống, quầng xám phía trên và quầng xanh wellness phía dưới, nở / thu theo [breath] (0..1). */
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
    glow(Calm.NeutralSoft.copy(alpha = 0.30f + 0.10f * breath), w * 0.12f, h * 0.10f, w * 0.85f * scale)
    glow(Calm.Wellness.copy(alpha = 0.07f + 0.04f * breath), w * 0.95f, h * 0.88f, w * 0.75f * scale)
    glow(Calm.Border.copy(alpha = 0.35f), w * 0.95f, h * 0.32f, w * 0.5f)
}
