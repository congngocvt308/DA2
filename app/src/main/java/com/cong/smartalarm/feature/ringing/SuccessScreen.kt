package com.cong.smartalarm.feature.ringing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.theme.Wake
import com.cong.smartalarm.core.designsystem.theme.rememberReducedMotion
import kotlin.math.ceil

/** Số giây trước khi màn hoàn thành tự đóng. */
private const val CloseAfterSeconds = 5

/**
 * Màn hoàn thành theo thiết kế "Màn hình hoàn thành v2": mặt trời đã thức, nút đếm ngược tự đóng.
 * Chạm nút để giữ màn hình sáng (dừng đếm); chạm lần nữa thì đếm lại từ đầu.
 */
@Composable
fun SuccessScreen(
    mission: WakeMission = WakeMission.None,
    onFinished: () -> Unit
) {
    LightSystemBars()
    val reducedMotion = rememberReducedMotion()
    val currentOnFinished by rememberUpdatedState(onFinished)

    var hold by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(hold) {
        progress.snapTo(0f)
        if (!hold) {
            progress.animateTo(1f, tween(CloseAfterSeconds * 1000, easing = LinearEasing))
            currentOnFinished()
        }
    }
    // Đang giữ thì màn hình không tự tối
    val view = LocalView.current
    DisposableEffect(hold) {
        view.keepScreenOn = hold
        onDispose { view.keepScreenOn = false }
    }
    val secondsLeft by remember {
        derivedStateOf { ceil(CloseAfterSeconds * (1f - progress.value)).toInt().coerceAtLeast(0) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wakeSky()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically)
        ) {
            HappySun(reducedMotion)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Dậy rồi nè!",
                    style = WakeText,
                    color = Wake.Ink,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    mission.cheer,
                    style = WakeText,
                    color = Wake.BodyText,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 260.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            WakePillButton(
                onClick = { hold = !hold },
                height = 60.dp,
                container = Wake.Ink,
                content = Wake.OnInk
            ) {
                // Thanh hổ phách chạy từ trái sang theo thời gian đếm (đọc ở pha vẽ, không recompose)
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(Wake.Amber, size = Size(size.width * progress.value, size.height))
                        }
                )
                Text(
                    if (hold) "Tiếp tục tắt màn hình" else "Tắt màn hình sau ${secondsLeft}s",
                    style = WakeText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                if (hold) "Màn hình sẽ luôn sáng" else "Chạm để giữ màn hình sáng",
                style = WakeText,
                color = Wake.HintText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun SuccessScreenPreview() {
    SuccessScreen(mission = WakeMission.Smile, onFinished = {})
}
