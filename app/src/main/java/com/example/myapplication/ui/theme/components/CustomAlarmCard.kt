package com.cong.smartalarm.ui.theme.components

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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

// Danh sách 20 cặp màu (Border, Neon Glow) phối sẵn
private val neonColorPalettes = listOf(
    // --- Phase 1: Classic & Cyberpunk ---
    Color(0xFFC084FC) to Color(0xFF80D8FF),
    Color(0xFF00897B) to Color(0xFFB2FF59),
    Color(0xFFE53935) to Color(0xFFFF8A80),
    Color(0xFF3F51B5) to Color(0xFF00E5FF),
    Color(0xFFFFB300) to Color(0xFFFFE082),
    Color(0xFFD81B60) to Color(0xFFFF4081),

    // --- Phase 2: Sunset & Warm Vibe ---
    Color(0xFFE65100) to Color(0xFFFF9100),
    Color(0xFF880E4F) to Color(0xFFFF1744),
    Color(0xFF4A148C) to Color(0xFFF50057),
    Color(0xFFBF360C) to Color(0xFFFFAB40),

    // --- Phase 3: Ocean & Cool Vibe ---
    Color(0xFF0D47A1) to Color(0xFF00B0FF),
    Color(0xFF004D40) to Color(0xFF1DE9B6),
    Color(0xFF1A237E) to Color(0xFF7C4DFF),
    Color(0xFF006064) to Color(0xFF18FFFF),

    // --- Phase 4: Candy & Pastel Neon ---
    Color(0xFF7B1FA2) to Color(0xFFEA80FC),
    Color(0xFFC2185B) to Color(0xFFFF80AB),
    Color(0xFF2E7D32) to Color(0xFF69F0AE),
    Color(0xFFF57C00) to Color(0xFFFFD700),

    // --- Phase 5: Deep Space & Mystery ---
    Color(0xFF311B92) to Color(0xFF651FFF),
    Color(0xFF263238) to Color(0xFF00E676)
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
                color = actualNeonColor,
                borderRadius = cornerRadius,
                blurRadius = 14.dp,
                spread = 3.dp
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
                            width = 1.5.dp,
                            color = Color.Black,
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
                    color = Color.Black,
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