package com.cong.smartalarm.feature.ringing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.theme.Nunito
import com.cong.smartalarm.core.designsystem.theme.Wake
import com.cong.smartalarm.core.designsystem.theme.rememberReducedMotion
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Màn chuông reo theo thiết kế "Màn hình chuông reo": trời kem đào, mặt trời đang ngủ, hai nút viên thuốc. */
@Composable
fun AlarmRingingScreen(
    alarmLabel: String,
    mission: WakeMission = WakeMission.None,
    isSnoozeEnabled: Boolean = true,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        val dateFormat = DateTimeFormatter.ofPattern("EEEE d 'Tháng' M", Locale("vi", "VN"))
        while (true) {
            val now = LocalDateTime.now()
            currentTime = now.format(timeFormat)
            currentDate = now.format(dateFormat)
            delay(1000)
        }
    }

    LightSystemBars()
    val reducedMotion = rememberReducedMotion()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wakeSky()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(44.dp))
        Text(
            text = currentDate.replaceFirstChar { it.uppercase() },
            style = WakeText,
            color = Wake.DateText,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = currentTime,
            style = WakeText.copy(fontFeatureSettings = "tnum", lineHeight = 96.sp),
            color = Wake.Ink,
            fontSize = 96.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.02).em
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically)
        ) {
            SleepySun(reducedMotion)
            MissionPill(mission.title ?: alarmLabel.ifBlank { "Báo thức" })
        }

        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSnoozeEnabled) {
                WakePillButton(
                    onClick = onSnooze,
                    height = 56.dp,
                    container = Color.Transparent,
                    content = Wake.Ink,
                    border = WakeButtonOutline
                ) {
                    Text("Báo lại", style = WakeText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            WakePillButton(
                onClick = onDismiss,
                height = 60.dp,
                container = Wake.Ink,
                content = Wake.OnInk
            ) {
                Text("Tắt báo thức", style = WakeText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

/** Kiểu chữ chung của hai màn đánh thức. */
internal val WakeText = TextStyle(fontFamily = Nunito)

@Composable
private fun MissionPill(text: String) {
    Row(
        modifier = Modifier
            .shadow(8.dp, CircleShape, ambientColor = Wake.PillShadow, spotColor = Wake.PillShadow)
            .background(Wake.Pill, CircleShape)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(10.dp)
                .background(Wake.Amber, CircleShape)
        )
        Text(
            text = text,
            style = WakeText,
            color = Wake.Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun AlarmRingingScreenPreview() {
    AlarmRingingScreen(alarmLabel = "Dậy đi học", mission = WakeMission.Smile, onSnooze = {}, onDismiss = {})
}
