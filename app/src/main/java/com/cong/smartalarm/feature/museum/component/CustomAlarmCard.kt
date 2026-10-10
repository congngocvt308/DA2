package com.cong.smartalarm.feature.museum.component

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.theme.Calm
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

// Các cặp màu pastel dịu (Viền, Quầng sáng nhẹ) — thay cho bảng neon cũ
private val neonColorPalettes = listOf(
    Color(0xFFE4E4E7) to Color(0xFF52525B), // xám
    Color(0xFFD4D4D8) to Color(0xFF18181B), // đen
    Color(0xFFD1FAE5) to Color(0xFF059669), // xanh wellness
    Color(0xFFA7F3D0) to Color(0xFF10B981), // bạc hà
    Color(0xFFDBEAFE) to Color(0xFF60A5FA), // xanh trời
    Color(0xFFFCE7F3) to Color(0xFFF472B6), // hồng phấn
    Color(0xFFFEF3C7) to Color(0xFFD97706), // vàng kem
    Color(0xFFF4F4F5) to Color(0xFF71717A)  // xám nhạt
)

fun Modifier.neonGlow(
    color: Color,
    borderRadius: Dp = 24.dp,
    blurRadius: Dp = 12.dp,
    spread: Dp = 2.dp
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            this.color = color.toArgb()
            maskFilter = BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
        }

        val left = -spread.toPx()
        val top = -spread.toPx()
        val right = size.width + spread.toPx()
        val bottom = size.height + spread.toPx()
        val radiusPx = borderRadius.toPx()

        canvas.nativeCanvas.drawRoundRect(
            left, top, right, bottom,
            radiusPx, radiusPx,
            paint
        )
    }
}

@Composable
fun CustomAlarmCard(
    imageUri: String,
    title: String,
    onCardClick: () -> Unit, // 🎯 Bấm vào đâu trên Card cũng mở Dialog
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    neonColor: Color? = null,
    cornerRadius: Dp = 24.dp,
    textCornerRadius: Dp = 14.dp,
    imageHeight: Dp = 180.dp,
    innerPadding: Dp = 8.dp,
    whiteMixRatio: Float = 0.82f // 🔴 Trả về file mới sau khi đổi tên
) {
    // Random cặp màu hợp tông và giữ nguyên qua remember
    val randomPalette = remember { neonColorPalettes.random() }
    val actualBorderColor = borderColor ?: randomPalette.first
    val actualNeonColor = neonColor ?: randomPalette.second

    val lightTextBackgroundColor = lerp(actualBorderColor, Color.White, whiteMixRatio)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .neonGlow(
                // Quầng sáng rất nhẹ thay cho neon chói
                color = actualNeonColor.copy(alpha = 0.22f),
                borderRadius = cornerRadius,
                blurRadius = 16.dp,
                spread = 1.dp
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(actualBorderColor)
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. PHẦN TRÊN: AsyncImage load từ File (Bấm vào để xem chi tiết)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding, start = innerPadding, end = innerPadding, bottom = 8.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 1.dp,
                            color = Calm.LavenderSoft,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            // 2. PHẦN DƯỚI: Ô Text (Bấm vào để sửa & Đổi tên file)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = textCornerRadius,
                            topEnd = textCornerRadius,
                            bottomStart = cornerRadius,
                            bottomEnd = cornerRadius
                        )
                    )
                    .background(lightTextBackgroundColor)
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title.ifBlank { "Kỷ niệm" },
                    color = Calm.Foreground,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    maxLines = Int.MAX_VALUE,
                    softWrap = true
                )
            }
        }
    }
}