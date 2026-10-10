package com.cong.smartalarm.feature.smile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.designsystem.theme.Calm

/** Thanh tiến trình bo tròn vẽ bằng Canvas: rãnh xám, phần đã đạt tô dải màu dịu, viền xám nhạt. */
@Composable
fun CustomPaintProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 3.dp // Bóp nhỏ borderWidth lại một chút cho thanh thoát giống Ảnh 2
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth() // Cho tràn full chiều rộng Box
                .height(40.dp)
        ) {
            val strokePx = borderWidth.toPx()

            val innerLeft = strokePx
            val innerTop = strokePx
            val innerWidth = size.width - (strokePx * 2)
            val innerHeight = size.height - (strokePx * 2)

            val innerRadius = innerHeight / 2

            // Dải màu dịu: xám -> đen -> xanh wellness
            val calmBrush = Brush.linearGradient(
                colors = listOf(Calm.Neutral, Calm.Primary, Calm.Wellness),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f)
            )

            if (innerWidth > 0f && innerHeight > 0f) {
                // 1. NỀN BÊN TRONG: rãnh xám nhạt
                drawRoundRect(
                    color = Calm.NeutralMist,
                    topLeft = Offset(innerLeft, innerTop),
                    size = Size(innerWidth, innerHeight),
                    cornerRadius = CornerRadius(innerRadius, innerRadius)
                )

                // 2. TIẾN TRÌNH (FILL)
                val currentProgressWidth = innerWidth * progress.coerceIn(0f, 1f)
                if (currentProgressWidth > 0f) {
                    drawRoundRect(
                        brush = calmBrush,
                        topLeft = Offset(innerLeft, innerTop),
                        size = Size(currentProgressWidth, innerHeight),
                        cornerRadius = CornerRadius(innerRadius, innerRadius)
                    )
                }
            }

            // 3. ĐƯỜNG VIỀN: viền xám nhạt
            val borderLeft = strokePx / 2
            val borderTop = strokePx / 2
            val borderWidthSize = size.width - strokePx
            val borderHeightSize = size.height - strokePx
            val borderRadius = borderHeightSize / 2

            drawRoundRect(
                color = Calm.NeutralSoft,
                topLeft = Offset(borderLeft, borderTop),
                size = Size(borderWidthSize, borderHeightSize),
                cornerRadius = CornerRadius(borderRadius, borderRadius),
                style = Stroke(width = strokePx)
            )
        }
    }
}
