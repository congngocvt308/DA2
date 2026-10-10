package com.cong.smartalarm.ui.theme.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.ui.theme.theme.Calm

/**
 * Bề mặt "soft UI" (neumorphism) cho Jetpack Compose — phong cách Mental Health App.
 *
 * Mỗi bề mặt có hai bóng mềm: bóng xám lệch xuống-phải và bóng trắng lệch lên-trái,
 * cộng một dải sáng lồi rất nhẹ, như được nặn nổi lên từ nền. Khi nhấn, bóng thu lại
 * (bề mặt "lún" xuống) trong 150 ms — không nảy, không rung, để giữ cảm giác yên tĩnh.
 *
 * Tên LiquidGlass/GlassBackdrop được giữ để các màn hình cũ không phải đổi;
 * các tham số kính (blur, refraction, light, backdrop) không còn tác dụng.
 */
@Stable
class GlassBackdrop(drawBackground: DrawScope.(canvasSize: Size) -> Unit) {
    var drawBackground by mutableStateOf(drawBackground)
    internal var origin by mutableStateOf(Offset.Zero)
    internal var size by mutableStateOf(Size.Zero)
}

/** Hậu cảnh chung của app (do [GlassHost] cung cấp). null = màn có camera/ảnh phía sau. */
val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdrop?> { null }

/** Giữ cho tương thích; nguồn sáng của soft UI cố định ở góc trên-trái. */
val LocalGlassLight = staticCompositionLocalOf<() -> Offset> { { DefaultLight } }

private val DefaultLight = Offset(-0.5f, -0.85f)

@Composable
fun rememberGlassBackdrop(drawBackground: DrawScope.(canvasSize: Size) -> Unit): GlassBackdrop {
    val backdrop = remember { GlassBackdrop(drawBackground) }
    backdrop.drawBackground = drawBackground
    return backdrop
}

/** Gắn vào layout gốc: vẽ hậu cảnh. */
fun Modifier.glassBackdropHost(backdrop: GlassBackdrop): Modifier = this
    .onGloballyPositioned {
        backdrop.origin = it.positionOnScreen()
        backdrop.size = Size(it.size.width.toFloat(), it.size.height.toFloat())
    }
    .drawBehind { backdrop.drawBackground(this, size) }

/** Lớp phủ mặc định: không nhuộm, giữ bề mặt trắng. */
val DefaultGlassTint: Brush = Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

/** Thời gian "lún" khi nhấn (soft press). */
internal const val SoftPressMillis = 150

/**
 * Hai bóng neumorphism, vẽ phía sau bề mặt. [depth] = 0 khi đang nhấn hết, 1 khi nổi.
 * setShadowLayer trên canvas tăng tốc phần cứng chỉ chạy với hình khối từ API 28.
 */
internal fun Modifier.softShadows(shape: Shape, elevation: Dp, depth: () -> Float): Modifier =
    if (elevation <= 0.dp) this
    else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) this.shadow(
        elevation * 0.6f, shape,
        ambientColor = Calm.ShadowDark.copy(alpha = 0.25f),
        spotColor = Calm.ShadowDark.copy(alpha = 0.35f)
    )
    else this.drawBehind {
        if (size.minDimension <= 0f) return@drawBehind
        val d = depth()
        val distance = (elevation * 0.42f).toPx() * (0.25f + 0.75f * d)
        val blur = (elevation * 1.25f).toPx() * (0.4f + 0.6f * d)
        val outline = shape.createOutline(size, layoutDirection, this)
        drawSoftShadow(outline, distance, distance, blur, Calm.ShadowDark.copy(alpha = 0.26f))
        drawSoftShadow(outline, -distance, -distance, blur, Calm.ShadowLight.copy(alpha = 0.95f))
    }

private fun DrawScope.drawSoftShadow(outline: Outline, dx: Float, dy: Float, blur: Float, color: Color) {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        paint.asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = Calm.Background.toArgb()
            setShadowLayer(blur, dx, dy, color.toArgb())
        }
        canvas.drawOutline(outline, paint)
    }
}

/**
 * Bề mặt nổi mềm.
 * @param cornerRadius bo góc; mặc định dạng viên thuốc (capsule). Bị bỏ qua nếu truyền [shape].
 * @param tint lớp màu phủ lên nền trắng (vd. nút màu chính, thẻ nhấn).
 * @param onClick nếu khác null, bề mặt bấm được và lún xuống khi nhấn.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = LocalGlassBackdrop.current,
    shape: Shape? = null,
    cornerRadius: Dp = Dp.Unspecified,
    blur: Dp = 10.dp,
    refraction: Dp = 16.dp,
    elevation: Dp = 14.dp,
    tint: Brush = DefaultGlassTint,
    light: () -> Offset = LocalGlassLight.current,
    contentAlignment: Alignment = Alignment.Center,
    propagateMinConstraints: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = LocalIndication.current,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceShape = shape ?: if (cornerRadius == Dp.Unspecified) RoundedCornerShape(50) else RoundedCornerShape(cornerRadius)
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed && onClick != null && enabled) 1f else 0f,
        animationSpec = tween(SoftPressMillis),
        label = "softPress"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interaction,
            indication = indication,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .softShadows(surfaceShape, elevation) { 1f - press }
            .clip(surfaceShape)
            .then(clickModifier),
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints
    ) {
        Box(
            Modifier
                .matchParentSize()
                .drawWithContent {
                    val stroke = 1.dp.toPx()
                    if (size.minDimension <= stroke * 2) {
                        drawContent()
                        return@drawWithContent
                    }
                    val outline = surfaceShape.createOutline(size, layoutDirection, this)
                    drawOutline(outline, Calm.Surface)
                    drawOutline(outline, tint)
                    // Lồi nhẹ: sáng ở góc trên-trái, tối rất nhẹ ở góc dưới-phải (145°)
                    drawOutline(
                        outline,
                        Brush.linearGradient(
                            0f to Color.White.copy(alpha = 0.22f),
                            0.5f to Color.Transparent,
                            1f to Calm.ShadowDark.copy(alpha = 0.06f),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        )
                    )
                    // Lún xuống khi nhấn: phủ một lớp xám rất nhạt
                    if (press > 0f) drawOutline(outline, Calm.ShadowDark.copy(alpha = 0.08f * press))
                    inset(stroke / 2) {
                        val rim = surfaceShape.createOutline(size, layoutDirection, this)
                        drawOutline(
                            rim,
                            Brush.linearGradient(
                                0f to Color.White.copy(alpha = 0.9f),
                                1f to Calm.ShadowDark.copy(alpha = 0.12f),
                                start = Offset.Zero,
                                end = Offset(size.width, size.height)
                            ),
                            style = Stroke(stroke)
                        )
                    }
                    drawContent()
                }
        )
        content()
    }
}

/** Nút nổi mềm: lún nhẹ khi nhấn (scale 0.98 + bóng thu lại), không nảy. */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdrop? = LocalGlassBackdrop.current,
    shape: Shape? = null,
    enabled: Boolean = true,
    tint: Brush = DefaultGlassTint,
    light: () -> Offset = LocalGlassLight.current,
    elevation: Dp = 10.dp,
    interactionSource: MutableInteractionSource? = null,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(SoftPressMillis),
        label = "softPressScale"
    )

    LiquidGlass(
        backdrop = backdrop,
        shape = shape,
        tint = tint,
        light = light,
        elevation = elevation,
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interaction,
        indication = null,
        propagateMinConstraints = propagateMinConstraints,
        modifier = modifier.graphicsLayer {
            scaleX = 1f - 0.02f * press
            scaleY = 1f - 0.02f * press
            alpha = if (enabled) 1f else 0.5f
        },
        content = content
    )
}

/** Hướng sáng theo độ nghiêng máy. Soft UI không dùng nữa; giữ lại cho mã cũ. */
@Composable
fun rememberTiltLight(): State<Offset> {
    val context = LocalContext.current
    val light = remember { mutableStateOf(DefaultLight) }
    DisposableEffect(context) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sm?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val tx = (-e.values[0] / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
                val ty = (e.values[1] / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
                val target = Offset(-0.5f + tx * 0.9f, -0.85f + (1f - ty) * 0.6f)
                light.value = light.value * 0.85f + target * 0.15f
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) = Unit
        }
        if (sm != null && sensor != null) sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm?.unregisterListener(listener) }
    }
    return light
}
