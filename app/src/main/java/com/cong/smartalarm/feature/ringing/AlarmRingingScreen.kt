package com.cong.smartalarm.feature.ringing

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
import com.cong.smartalarm.core.designsystem.theme.BreathCycleMillis
import com.cong.smartalarm.core.designsystem.component.SoftSurface
import com.cong.smartalarm.core.designsystem.component.SoftSurfaceButton
import com.cong.smartalarm.core.designsystem.theme.rememberReducedMotion
import com.cong.smartalarm.core.designsystem.theme.Dawn
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            // phase chỉ được đọc khi vẽ -> trời chuyển màu mà không recompose
            .drawBehind { drawSunrise(size, phase.value, stars) }
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
                color = Dawn.MutedText,
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
                            listOf(Color.White.copy(alpha = 0.45f + 0.2f * b), Color.White.copy(alpha = 0f)),
                            center = center,
                            radius = r
                        ),
                        radius = r
                    )
                    drawCircle(
                        Dawn.Sun.copy(alpha = 0.25f + 0.2f * b),
                        radius = r * 0.82f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            ) {
                Text(
                    text = currentTime,
                    color = Dawn.Text,
                    fontSize = 104.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-3).sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- NHÃN BÁO THỨC TRONG VIÊN KÍNH ---
            SoftSurface(
                tint = SolidColor(Dawn.Surface),
                elevation = 8.dp,
                modifier = Modifier.height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Dawn.SunIcon,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = alarmLabel.ifBlank { "Báo thức" },
                        color = Dawn.Text,
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
                SoftSurfaceButton(
                    onClick = onSnooze,
                    tint = SolidColor(Dawn.Surface),
                    modifier = Modifier
                        .width(200.dp)
                        .height(60.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Snooze, contentDescription = null, tint = Dawn.Text, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Báo lại", color = Dawn.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- NÚT TẮT BÁO THỨC: nâu đậm trên trời sáng -> nổi bật nhất màn hình ---
            SoftSurfaceButton(
                onClick = onNavigateToQuiz,
                tint = SolidColor(Dawn.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AlarmOff, contentDescription = null, tint = Dawn.SkyTop, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Tắt báo thức", color = Dawn.SkyTop, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Bầu trời bình minh: phía trên còn chút chàm của đêm, chuyển dần sang đào / cam ở chân trời,
// vài sao cuối cùng mờ dần và mặt trời ló lên ở đáy. Mọi chuyển động đều rất chậm.
// ---------------------------------------------------------------------------

private class Star(val x: Float, val y: Float, val r: Float, val twinkle: Float)

private fun generateStars(): List<Star> {
    val rnd = Random(7)
    return List(18) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.25f, 0.6f + rnd.nextFloat() * 1.2f, rnd.nextFloat() * 6.28f) }
}

private fun DrawScope.drawSunrise(canvas: Size, phase: Float, stars: List<Star>) {
    val w = canvas.width
    val h = canvas.height
    if (w <= 0f || h <= 0f) return

    drawRect(
        Brush.verticalGradient(
            0f to Color(0xFFC3C6F2),
            0.35f to Dawn.SkyTop,
            0.7f to Dawn.SkyMid,
            1f to Dawn.SkyHorizon,
            endY = h
        ),
        size = canvas
    )

    // Sao cuối cùng của đêm, mờ dần
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
    blob(Color(0xFFFFF1E0).copy(alpha = 0.45f), w * (0.2f + 0.05f * sin(phase)), h * (0.42f + 0.02f * cos(phase)), w * 0.7f)
    blob(Color(0xFFFFCFA0).copy(alpha = 0.35f), w * (0.85f + 0.04f * cos(phase)), h * (0.6f + 0.02f * sin(phase)), w * 0.6f)

    // Mặt trời mọc: quầng sáng ấm, viền mềm
    val sunCenter = Offset(w * 0.5f, h * (0.98f - 0.01f * sin(phase)))
    val sunR = w * 0.36f
    blob(Dawn.Sun.copy(alpha = 0.55f), sunCenter.x, sunCenter.y, sunR * 2.4f)
    drawCircle(
        Brush.radialGradient(
            listOf(Color(0xFFFFF8E7), Color(0xFFFFE0A3), Dawn.Sun.copy(alpha = 0f)),
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
