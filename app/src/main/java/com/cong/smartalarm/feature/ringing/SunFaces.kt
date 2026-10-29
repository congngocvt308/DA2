package com.cong.smartalarm.feature.ringing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.theme.Nunito
import com.cong.smartalarm.core.designsystem.theme.Wake

// Hai mặt trời vẽ theo khung 180 x 180 của thiết kế; mọi số đo bên dưới là dp trong khung đó.
private const val SunBox = 180f

/** Vẽ trong hệ toạ độ dp của khung 180 x 180 (nét vẽ cũng tính bằng dp). */
private inline fun DrawScope.inSunBox(block: DrawScope.() -> Unit) {
    val k = size.width / SunBox
    scale(k, k, pivot = Offset.Zero) { block() }
}

/** Mặt trời đang ngủ trên màn reo: hai vòng sáng loang ra, mặt lắc lư, "z" bay lên. */
@Composable
fun SleepySun(reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "SleepySun")
    // 0..1 trong 1,4 giây; vòng thứ hai lệch nửa nhịp
    val halo: State<Float> = if (reducedMotion) remember { mutableFloatStateOf(-1f) } else {
        transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "Halo")
    }
    val wobble: State<Float> = if (reducedMotion) remember { mutableFloatStateOf(0f) } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                keyframes {
                    durationMillis = 1400
                    0f at 0 using EaseInOut
                    -8f at 140 using EaseInOut
                    8f at 280 using EaseInOut
                    -6f at 420 using EaseInOut
                    6f at 560 using EaseInOut
                    0f at 700
                }
            ),
            label = "Wobble"
        )
    }
    val snore: State<Float> = if (reducedMotion) remember { mutableFloatStateOf(-1f) } else {
        transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "Snore")
    }

    Box(modifier.size(SunBox.dp)) {
        Canvas(Modifier.size(SunBox.dp)) {
            inSunBox {
                val c = Offset(90f, 90f)
                if (halo.value >= 0f) {
                    for (shift in floatArrayOf(0f, 0.5f)) {
                        val p = EaseOut.transform((halo.value + shift) % 1f)
                        drawCircle(
                            Wake.SunHalo.copy(alpha = 0.6f * (1f - p)),
                            radius = 88.5f * (0.9f + 0.6f * p),
                            center = c,
                            style = Stroke(3f)
                        )
                    }
                }
                rotate(wobble.value, pivot = c) {
                    drawCircle(Wake.Sun, radius = 90f, center = c)
                    // Mắt nhắm: nửa vòng cung úp xuống
                    for (x in floatArrayOf(52f, 106f)) {
                        drawArc(
                            Wake.Ink, 0f, 180f, useCenter = false,
                            topLeft = Offset(x + 2.5f, 72f - 8.5f), size = Size(17f, 17f),
                            style = Stroke(5f, cap = StrokeCap.Round)
                        )
                    }
                    for (x in floatArrayOf(34f, 124f)) {
                        drawOval(Wake.Cheek.copy(alpha = 0.7f), topLeft = Offset(x, 94f), size = Size(22f, 12f))
                    }
                    // Miệng tròn nhỏ đang ngáy
                    drawOval(Wake.Ink, topLeft = Offset(82f, 108f), size = Size(16f, 14f))
                }
            }
        }
        if (!reducedMotion) {
            SnoreZ(snore, delay = 0f, x = 160, y = 10, fontSize = 22)
            SnoreZ(snore, delay = 0.8f / 2.4f, x = 176, y = -8, fontSize = 16)
            SnoreZ(snore, delay = 1.6f / 2.4f, x = 150, y = -20, fontSize = 13)
        }
    }
}

/** Một chữ "z" bay chéo lên rồi mờ đi; [delay] tính theo phần của chu kỳ 2,4 giây. */
@Composable
private fun SnoreZ(progress: State<Float>, delay: Float, x: Int, y: Int, fontSize: Int) {
    Text(
        text = "z",
        color = Wake.Snore,
        fontFamily = Nunito,
        fontWeight = FontWeight.Black,
        fontSize = fontSize.sp,
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                layout(0, 0) { placeable.place(x.dp.roundToPx(), y.dp.roundToPx()) }
            }
            .graphicsLayer {
                val p = (progress.value - delay + 1f) % 1f
                val e = EaseOut.transform(p)
                translationX = 18.dp.toPx() * e
                translationY = -40.dp.toPx() * e
                alpha = if (p < 0.2f) p / 0.2f else 1f - (p - 0.2f) / 0.8f
            }
    )
}

private val PopIn = CubicBezierEasing(0.3f, 1.5f, 0.5f, 1f)

/** Mặt trời đã thức trên màn hoàn thành: bật ra, nhún nhẹ, mắt chớp. */
@Composable
fun HappySun(reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val pop = remember { Animatable(if (reducedMotion) 1f else 0f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, tween(500, easing = PopIn)) }

    val transition = rememberInfiniteTransition(label = "HappySun")
    val bob: State<Float> = if (reducedMotion) remember { mutableFloatStateOf(0f) } else {
        transition.animateFloat(
            0f, -10f,
            infiniteRepeatable(tween(1300, easing = EaseInOut), RepeatMode.Reverse),
            label = "Bob"
        )
    }
    // Mắt mở suốt, chỉ khép rồi mở lại trong 8% cuối của 4 giây
    val blink: State<Float> = if (reducedMotion) remember { mutableFloatStateOf(1f) } else {
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                keyframes {
                    durationMillis = 4000
                    1f at 3680
                    0.1f at 3840
                    1f at 4000
                }
            ),
            label = "Blink"
        )
    }

    Canvas(
        modifier
            .size(SunBox.dp)
            .graphicsLayer {
                val p = pop.value
                scaleX = 0.6f + 0.4f * p
                scaleY = 0.6f + 0.4f * p
                alpha = p.coerceIn(0f, 1f)
                translationY = bob.value * density
            }
    ) {
        inSunBox {
            val c = Offset(90f, 90f)
            drawCircle(Wake.SunGlow.copy(alpha = 0.45f), radius = 108f, center = c)
            drawCircle(Wake.Sun, radius = 90f, center = c)
            for (x in floatArrayOf(56f, 110f)) {
                withTransform({ scale(1f, blink.value, pivot = Offset(x + 7f, 79f)) }) {
                    drawOval(Wake.Ink, topLeft = Offset(x, 70f), size = Size(14f, 18f))
                }
            }
            for (x in floatArrayOf(34f, 124f)) {
                drawOval(Wake.Cheek.copy(alpha = 0.7f), topLeft = Offset(x, 96f), size = Size(22f, 12f))
            }
            // Miệng cười: nửa vòng cung ngửa lên
            drawArc(
                Wake.Ink, 0f, 180f, useCenter = false,
                topLeft = Offset(90f - 15.5f, 92f - 15.5f), size = Size(31f, 31f),
                style = Stroke(5f, cap = StrokeCap.Round)
            )
        }
    }
}
