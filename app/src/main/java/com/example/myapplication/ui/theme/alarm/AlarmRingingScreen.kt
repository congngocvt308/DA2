package com.cong.smartalarm.ui.theme.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.ui.theme.components.BreathCycleMillis
import com.cong.smartalarm.ui.theme.components.LiquidGlass
import com.cong.smartalarm.ui.theme.components.LiquidGlassButton
import com.cong.smartalarm.ui.theme.components.glassBackdropHost
import com.cong.smartalarm.ui.theme.components.rememberGlassBackdrop
import com.cong.smartalarm.ui.theme.components.rememberReducedMotion
import com.cong.smartalarm.ui.theme.components.rememberTiltLight
import com.cong.smartalarm.ui.theme.theme.Calm
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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

    // Giảm chuyển động (Trợ năng) -> nền và vòng thở đứng yên
    val reducedMotion = rememberReducedMotion()
    val infiniteTransition = rememberInfiniteTransition(label = "Calm")
    // 🌅 Pha trôi rất chậm của nền bình minh (đọc trong draw để không recompose mỗi frame)
    val phase: State<Float> = if (reducedMotion) {
        remember { mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing)),
            label = "Phase"
        )
    }
    // 🌬️ Nhịp thở quanh đồng hồ: hít 4 giây, thở ra 4 giây
    val breath: State<Float> = if (reducedMotion) {
        remember { mutableFloatStateOf(0.5f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween((BreathCycleMillis / 2).toInt(), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Breath"
        )
    }

    val stars = remember { generateStars() }
    val backdrop = rememberGlassBackdrop { canvasSize -> drawSunrise(canvasSize, phase.value, stars) }
    val tilt = rememberTiltLight()
    val light = { tilt.value }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassBackdropHost(backdrop)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = currentDate.replaceFirstChar { it.uppercase() },
                color = Calm.MutedForeground,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Đồng hồ với quầng sáng "thở" nhẹ phía sau
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.drawBehind {
                    val b = breath.value
                    val r = size.width * 0.5f * (0.92f + 0.06f * b)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Calm.LavenderMist.copy(alpha = 0.55f + 0.2f * b), Calm.LavenderMist.copy(alpha = 0f)),
                            center = center,
                            radius = r
                        ),
                        radius = r
                    )
                    drawCircle(
                        Calm.Lavender.copy(alpha = 0.10f + 0.10f * b),
                        radius = r * 0.82f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            ) {
                Text(
                    text = currentTime,
                    color = Calm.Foreground,
                    fontSize = 104.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-3).sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- NHÃN BÁO THỨC TRONG VIÊN KÍNH ---
            LiquidGlass(
                backdrop = backdrop,
                light = light,
                modifier = Modifier.height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Calm.Warm,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = alarmLabel.ifBlank { "Báo thức" },
                        color = Calm.Foreground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- NÚT BÁO LẠI (Chỉ hiện nếu bật) ---
            if (isSnoozeEnabled) {
                LiquidGlassButton(
                    onClick = onSnooze,
                    backdrop = backdrop,
                    light = light,
                    modifier = Modifier
                        .width(200.dp)
                        .height(60.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Snooze, contentDescription = null, tint = Calm.Primary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Báo lại", color = Calm.Foreground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- NÚT TẮT BÁO THỨC: NÚT CHÍNH MÀU TÍM ---
            LiquidGlassButton(
                onClick = onNavigateToQuiz,
                backdrop = backdrop,
                light = light,
                tint = SolidColor(Calm.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AlarmOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Tắt báo thức", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Nền bình minh dịu: trời xám bạc nhạt chuyển dần sang hồng đào / kem ở chân trời,
// vài đốm sáng mờ và mặt trời toả sáng nhẹ. Mọi chuyển động đều rất chậm.
// ---------------------------------------------------------------------------

private class Star(val x: Float, val y: Float, val r: Float, val twinkle: Float)

private fun generateStars(): List<Star> {
    val rnd = Random(7)
    return List(28) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.45f, 0.6f + rnd.nextFloat() * 1.4f, rnd.nextFloat() * 6.28f) }
}

private fun DrawScope.drawSunrise(canvas: Size, phase: Float, stars: List<Star>) {
    val w = canvas.width
    val h = canvas.height
    if (w <= 0f || h <= 0f) return

    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFFF4F4F5),
            0.4f to Color(0xFFF7F7F8),
            0.72f to Color(0xFFFDEFF0),
            1f to Color(0xFFFFE9D6),
            endY = h
        ),
        size = canvas
    )

    // Đốm sáng mờ còn sót lại của đêm, lấp lánh rất chậm
    stars.forEach { s ->
        val a = 0.35f + 0.25f * (0.5f + 0.5f * sin(phase * 4f + s.twinkle))
        drawCircle(Color.White.copy(alpha = a), radius = s.r * density, center = Offset(s.x * w, s.y * h))
    }

    fun blob(color: Color, cx: Float, cy: Float, radius: Float) {
        drawCircle(
            Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center = Offset(cx, cy), radius = radius),
            radius = radius,
            center = Offset(cx, cy)
        )
    }
    blob(Calm.LavenderSoft.copy(alpha = 0.35f), w * (0.2f + 0.05f * sin(phase)), h * (0.22f + 0.02f * cos(phase)), w * 0.7f)
    blob(Color(0xFFE4E4E7).copy(alpha = 0.30f), w * (0.85f + 0.04f * cos(phase)), h * (0.55f + 0.02f * sin(phase)), w * 0.6f)
    blob(Color(0xFFE4E4E7).copy(alpha = 0.30f), w * (0.55f + 0.06f * sin(phase + 2f)), h * 0.08f, w * 0.55f)

    // Mặt trời mọc: quầng sáng ấm, viền mềm
    val sunCenter = Offset(w * 0.5f, h * (0.95f - 0.01f * sin(phase)))
    val sunR = w * 0.34f
    blob(Color(0xFFFFD8B5).copy(alpha = 0.55f), sunCenter.x, sunCenter.y, sunR * 2.4f)
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFFFFF4E0), Color(0xFFFFE2C2), Color(0xFFFFD3B0).copy(alpha = 0f)),
            center = sunCenter,
            radius = sunR
        ),
        radius = sunR,
        center = sunCenter
    )
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun AlarmRingingScreenPreview() {
    MaterialTheme {
        AlarmRingingScreen(alarmLabel = "Dậy đi học", onSnooze = {}, onNavigateToQuiz = {})
    }
}
