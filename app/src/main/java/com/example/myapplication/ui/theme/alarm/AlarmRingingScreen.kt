package com.cong.smartalarm.ui.theme.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AlarmRingingScreen(
    alarmLabel: String,
    hasQRCodes: Boolean = false,
    isSnoozeEnabled: Boolean = true,
    onSnooze: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToQRScan: () -> Unit = {}
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = LocalDateTime.now()
            currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"))
            val dateFormatter = DateTimeFormatter.ofPattern("EEEE dd 'Tháng' MM", Locale("vi", "VN"))
            currentDate = now.format(dateFormatter)
            delay(1000)
        }
    }

    // 🎯 HIỆU ỨNG NHẤP NHÁY CHO NÚT TẮT
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    // 🎨 BACKGROUND GRADIENT HIỆN ĐẠI
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1E2C), // Tím than đậm
            Color(0xFF000000)  // Đen
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(vertical = 60.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = currentDate,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = alarmLabel,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- NÚT BÁO LẠI (Chỉ hiện nếu bật) ---
        if (isSnoozeEnabled) {
            Button(
                onClick = onSnooze,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .width(180.dp)
                    .height(56.dp)
            ) {
                Text("Báo lại", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- NÚT TẮT BÁO THỨC (CÓ HIỆU ỨNG) ---
        Button(
            onClick = onNavigateToQuiz,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .scale(scale) // Hiệu ứng phình to
        ) {
            Text("Tắt báo thức", color = MaterialTheme.colorScheme.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
