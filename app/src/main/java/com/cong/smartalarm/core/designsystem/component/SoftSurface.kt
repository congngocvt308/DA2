package com.cong.smartalarm.core.designsystem.component

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.core.designsystem.theme.SoftPressMillis

/**
 * Bề mặt "soft UI" trên nền đêm: bóng tối mềm đổ xuống dưới, viền sáng rất mảnh phía trên
 * (như ánh trăng hắt lên mép) và dải lồi nhẹ, để thẻ nổi khỏi bầu trời mà không chói.
 * Khi nhấn, bóng thu lại (bề mặt "lún") trong [SoftPressMillis] — không nảy.
 *
 * Hiệu năng: Outline, Paint và Brush được tạo một lần mỗi khi kích thước đổi (drawWithCache);
 * mỗi khung hình chỉ vẽ lại, và trạng thái nhấn chỉ được đọc ở pha vẽ nên không gây recompose.
 */

/** Không phủ màu: giữ bề mặt trắng. */
val NoTint: Brush = SolidColor(Color.Transparent)

/**
 * Hai bóng neumorphism vẽ phía sau bề mặt. [depth] = 1 khi nổi, 0 khi đã lún hết.
 * setShadowLayer với hình khối chỉ chạy trên canvas tăng tốc phần cứng từ API 28; máy cũ hơn dùng shadow thường.
 */
internal fun Modifier.softShadows(shape: Shape, elevation: Dp, depth: () -> Float = { 1f }): Modifier =
    if (elevation <= 0.dp) this
    else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) this.shadow(
        elevation * 0.6f, shape,
        ambientColor = Calm.ShadowDark.copy(alpha = 0.25f),
        spotColor = Calm.ShadowDark.copy(alpha = 0.35f)
    )
    else this.drawWithCache {
        val outline = shape.createOutline(size, layoutDirection, this)
        val darkColor = Calm.ShadowDark.copy(alpha = 0.6f).toArgb()
        // Trên nền tối, bóng sáng chỉ còn như một vầng rất nhạt — mạnh hơn sẽ thành vệt bẩn
        val lightColor = Calm.ShadowLight.copy(alpha = 0.035f).toArgb()
        val paint = Paint().also {
            it.asFrameworkPaint().isAntiAlias = true
            it.asFrameworkPaint().color = Calm.Background.toArgb()
        }
        val maxDistance = (elevation * 0.42f).toPx()
        val maxBlur = (elevation * 1.25f).toPx()
        onDrawBehind {
            if (size.minDimension <= 0f) return@onDrawBehind
            val d = depth()
            val distance = maxDistance * (0.25f + 0.75f * d)
            val blur = maxBlur * (0.4f + 0.6f * d)
            val framework = paint.asFrameworkPaint()
            drawIntoCanvas { canvas ->
                framework.setShadowLayer(blur, distance * 0.4f, distance * 1.2f, darkColor)
                canvas.drawOutline(outline, paint)
                framework.setShadowLayer(blur, -distance * 0.4f, -distance, lightColor)
                canvas.drawOutline(outline, paint)
            }
        }
    }

/** Lớp nền + màu phủ + dải lồi + viền của bề mặt; [press] (0..1) chỉ đọc khi vẽ. */
private fun Modifier.softFace(shape: Shape, tint: Brush, press: () -> Float): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val stroke = 1.dp.toPx()
    val convex = Brush.linearGradient(
        0f to Color.White.copy(alpha = 0.05f),
        0.5f to Color.Transparent,
        1f to Calm.ShadowDark.copy(alpha = 0.18f),
        start = Offset.Zero,
        end = Offset(0f, size.height)
    )
    // Viền "ánh trăng": sáng ở mép trên, tan dần xuống dưới
    val rimBrush = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.12f),
        0.4f to Color.White.copy(alpha = 0.04f),
        1f to Color.White.copy(alpha = 0.02f)
    )
    val canDrawRim = size.minDimension > stroke * 2
    val rim = if (canDrawRim) {
        // Viền vẽ lùi vào nửa nét để không bị cắt
        val inner = Size(size.width - stroke, size.height - stroke)
        shape.createOutline(inner, layoutDirection, this)
    } else null
    onDrawBehind {
        drawOutline(outline, Calm.Surface)
        drawOutline(outline, tint)
        drawOutline(outline, convex)
        val p = press()
        if (p > 0f) drawOutline(outline, Calm.ShadowDark.copy(alpha = 0.18f * p))
        if (rim != null) translate(stroke / 2, stroke / 2) { drawOutline(rim, rimBrush, style = Stroke(stroke)) }
    }
}

/**
 * Bề mặt nổi mềm.
 * @param cornerRadius bo góc; mặc định dạng viên thuốc. Bị bỏ qua nếu truyền [shape].
 * @param tint lớp màu phủ lên nền trắng (vd. nút màu chính).
 * @param onClick khác null thì bề mặt bấm được và lún xuống khi nhấn.
 */
@Composable
fun SoftSurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    cornerRadius: Dp = Dp.Unspecified,
    elevation: Dp = 14.dp,
    tint: Brush = NoTint,
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
    val press = animateFloatAsState(
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
            .softShadows(surfaceShape, elevation) { 1f - press.value }
            .clip(surfaceShape)
            .softFace(surfaceShape, tint) { press.value }
            .then(clickModifier),
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
        content = content
    )
}

/** Nút nổi mềm: lún nhẹ khi nhấn (scale 0.98 + bóng thu lại), không nảy. */
@Composable
fun SoftSurfaceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    enabled: Boolean = true,
    tint: Brush = NoTint,
    elevation: Dp = 10.dp,
    interactionSource: MutableInteractionSource? = null,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val press = animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = tween(SoftPressMillis),
        label = "softPressScale"
    )

    SoftSurface(
        shape = shape,
        tint = tint,
        elevation = elevation,
        onClick = onClick,
        enabled = enabled,
        role = Role.Button,
        interactionSource = interaction,
        indication = null,
        propagateMinConstraints = propagateMinConstraints,
        modifier = modifier.graphicsLayer {
            val s = 1f - 0.02f * press.value
            scaleX = s
            scaleY = s
            alpha = if (enabled) 1f else 0.5f
        },
        content = content
    )
}
