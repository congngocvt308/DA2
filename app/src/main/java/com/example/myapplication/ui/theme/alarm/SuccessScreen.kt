package com.cong.smartalarm.ui.theme.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(
    onFinished: () -> Unit
) {
    // 🎯 Tự động tắt sau 3.5 giây
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SuccessPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconScale"
    )

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFD54F), Color(0xFFFF8A65))
    )

    Box(
        modifier = Modifier.fillMaxSize().background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(100.dp).scale(scale))
            Spacer(modifier = Modifier.height(32.dp))
            Text("KHỞI ĐẦU TUYỆT VỜI!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bạn đã chinh phục thử thách.\nChúc bạn một ngày tràn đầy năng lượng! 🚀", color = Color.White.copy(alpha = 0.9f), fontSize = 18.sp, textAlign = TextAlign.Center, lineHeight = 26.sp)
        }
    }
}
