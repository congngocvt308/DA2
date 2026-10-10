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
 * Nền gốc của cả app: bầu trời đêm có quầng bình minh "thở" chậm (nhịp [BreathCycleMillis]).
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

private class Star(val x: Float, val y: Float, val r: Float, val phase: Float)

// Sao cố định (seed cố định -> không nhảy vị trí giữa các lần vẽ), chỉ ở nửa trên bầu trời
private val stars = kotlin.random.Random(7).let { rnd ->
    List(36) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.55f, 0.6f + rnd.nextFloat() * 0.9f, rnd.nextFloat()) }
}

/**
 * Bầu trời đêm: xanh đêm đậm dần xuống, quầng chàm phía trên, sao mờ lấp lánh theo nhịp thở,
 * và quầng hổ phách "bình minh" ló ở đáy, nở / thu theo [breath] (0..1).
 */
fun DrawScope.drawCalmBackground(canvas: Size, breath: Float) {
    val w = canvas.width
    val h = canvas.height
    if (w <= 0f || h <= 0f) return

    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFF0B1224),
            0.55f to Calm.Background,
            1f to Color(0xFF141B33),
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
    glow(Calm.Accent.copy(alpha = 0.14f), w * 0.08f, 0f, w * 0.9f)

    stars.forEach { s ->
        // Mỗi sao lệch pha một chút để không nhấp nháy đồng loạt
        val twinkle = 0.5f + 0.5f * kotlin.math.sin((breath + s.phase) * PI.toFloat())
        drawCircle(Color.White.copy(alpha = 0.18f + 0.30f * twinkle), radius = s.r * density, center = Offset(s.x * w, s.y * h))
    }

    val scale = 0.92f + 0.08f * breath
    glow(Calm.Primary.copy(alpha = 0.16f + 0.06f * breath), w * 0.5f, h * 1.06f, w * 1.1f * scale)
}
