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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.ui.theme.components.BreathCycleMillis
import com.cong.smartalarm.ui.theme.components.rememberReducedMotion
import com.cong.smartalarm.ui.theme.theme.Calm
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

    // 🌬️ Biểu tượng "thở" nhẹ theo nhịp 8 giây; đứng yên nếu bật giảm chuyển động
    val reducedMotion = rememberReducedMotion()
    val scale = if (reducedMotion) 1f else {
        val infiniteTransition = rememberInfiniteTransition(label = "SuccessBreath")
        val s by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween((BreathCycleMillis / 2).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "IconScale"
        )
        s
    }

    // Nền pastel của app hiện qua (không còn gradient vàng cam)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Calm.WellnessMist),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Calm.Wellness, modifier = Modifier.size(72.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("KHỞI ĐẦU TUYỆT VỜI!", color = Calm.Foreground, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bạn đã chinh phục thử thách.\nChúc bạn một ngày tràn đầy năng lượng! 🚀", color = Calm.MutedForeground, fontSize = 18.sp, textAlign = TextAlign.Center, lineHeight = 26.sp)
        }
    }
}
