package com.cong.smartalarm.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.core.designsystem.theme.SoftPressMillis

/**
 * Thanh kéo soft UI dùng chung cho cả app (thay cho Material3 Slider).
 * - Rãnh dày 10dp bo tròn, phần đã chọn tô dải chàm -> hổ phách, rãnh trống tối có viền mảnh.
 * - Nút kéo sáng nổi (bóng mềm), tâm hổ phách; phồng nhẹ khi đang kéo.
 * - Có [steps] thì vẽ chấm mốc trên rãnh (bỏ qua nếu quá dày).
 * Vùng chạm vẫn ≥ 48dp như Slider gốc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalmSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val dragged by interaction.collectIsDraggedAsState()
    val pressed by interaction.collectIsPressedAsState()
    val active by animateFloatAsState(
        targetValue = if (dragged || pressed) 1f else 0f,
        animationSpec = tween(SoftPressMillis),
        label = "sliderThumb"
    )

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interaction,
        thumb = { CalmSliderThumb(active) },
        track = { state -> CalmSliderTrack(state) }
    )
}

@Composable
private fun CalmSliderThumb(active: Float) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .graphicsLayer {
                scaleX = 1f + 0.12f * active
                scaleY = 1f + 0.12f * active
            }
            .softShadows(CircleShape, 6.dp) { 1f }
            .drawBehind {
                val r = size.minDimension / 2
                drawCircle(Calm.Foreground, r)
                drawCircle(Calm.Subtle, r - 0.5.dp.toPx(), style = Stroke(1.dp.toPx()))
                // Tâm hổ phách nở ra khi kéo: báo hiệu đang giữ
                drawCircle(Calm.Primary, r * (0.34f + 0.08f * active))
            },
        contentAlignment = Alignment.Center
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalmSliderTrack(state: SliderState) {
    val range = state.valueRange
    val span = (range.endInclusive - range.start).takeIf { it > 0f } ?: 1f
    val fraction = ((state.value - range.start) / span).coerceIn(0f, 1f)
    val steps = state.steps

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val h = size.height
        val radius = CornerRadius(h / 2, h / 2)
        // Rãnh trống: tối, viền mảnh như được khắc lõm vào bề mặt
        drawRoundRect(Calm.Raised, cornerRadius = radius)
        drawRoundRect(
            Calm.Border,
            topLeft = Offset(0.5f, 0.5f),
            size = Size(size.width - 1f, h - 1f),
            cornerRadius = radius,
            style = Stroke(1.dp.toPx())
        )
        // Phần đã chọn: dải chàm -> hổ phách
        val activeWidth = size.width * fraction
        if (activeWidth > 0f) {
            drawRoundRect(
                Brush.horizontalGradient(listOf(Calm.Accent, Calm.Primary), endX = activeWidth.coerceAtLeast(1f)),
                size = Size(activeWidth.coerceAtLeast(h), h),
                cornerRadius = radius
            )
        }
        // Chấm mốc
        if (steps in 1..20) {
            val dot = 1.6.dp.toPx()
            for (i in 0..steps + 1) {
                val x = size.width * i / (steps + 1)
                val onActive = x <= activeWidth
                drawCircle(
                    color = if (onActive) Calm.OnPrimary.copy(alpha = 0.7f) else Calm.MutedForeground,
                    radius = dot,
                    center = Offset(x.coerceIn(h / 2, size.width - h / 2), h / 2)
                )
            }
        }
    }
}
