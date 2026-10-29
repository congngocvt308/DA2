package com.cong.smartalarm.feature.alarmedit

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.theme.Calm
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs


/** Index giữa danh sách "vô hạn" ứng với [value] (chia hết cho [count] để index % count == value). */
internal fun wheelStartIndex(value: Int, count: Int): Int {
    val base = Int.MAX_VALUE / 2
    return base - (base % count) + value
}

private val WheelItemHeight = 48.dp
private val WheelViewportHeight = 150.dp

/** Bánh xe chọn giờ : phút cuộn vô hạn; phút qua mốc 59 <-> 00 thì giờ đổi theo. */
@Composable
fun TimePickerSection(
    hourListState: LazyListState,
    minuteListState: LazyListState,
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
){
    val scope = rememberCoroutineScope()
    // Index tuyệt đối lúc bánh xe dừng lần trước: so "vòng" (index / 60) để biết phút đã qua mốc 59 <-> 00
    var settledHourIndex by remember { mutableStateOf(hourListState.firstVisibleItemIndex) }
    var settledMinuteIndex by remember { mutableStateOf(minuteListState.firstVisibleItemIndex) }

    RememberInfiniteSnap(hourListState) { index ->
        settledHourIndex = index
        onHourChange(index % 24)
    }
    RememberInfiniteSnap(minuteListState) { index ->
        val laps = Math.floorDiv(index, 60) - Math.floorDiv(settledMinuteIndex, 60)
        settledMinuteIndex = index
        onMinuteChange(index % 60)
        // Kéo lên qua 59 -> 00: giờ +1; kéo xuống qua 00 -> 59: giờ -1 (vuốt mạnh nhiều vòng thì nhiều giờ)
        if (laps != 0) {
            val hourIndex = settledHourIndex + laps
            settledHourIndex = hourIndex
            onHourChange(Math.floorMod(hourIndex, 24))
            scope.launch { hourListState.animateScrollToItem(hourIndex) }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WheelViewportHeight + 40.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dải chọn ở giữa, kiểu bánh xe iOS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Calm.Raised)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(WheelViewportHeight)
                // Mờ dần hai mép trên/dưới
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.35f to Color.Black,
                            0.65f to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfiniteWheelColumn(
                state = hourListState,
                itemCount = 24,
                selectedItem = selectedHour,
                alignment = Alignment.End
            )

            // Dấu ":" nằm trong ô cao bằng ô số, cùng kiểu chữ -> thẳng hàng với số đang chọn
            Box(
                modifier = Modifier
                    .height(WheelItemHeight)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "case"),
                    color = Calm.Foreground,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            InfiniteWheelColumn(
                state = minuteListState,
                itemCount = 60,
                selectedItem = selectedMinute,
                alignment = Alignment.Start
            )
        }
    }
}

@Composable
private fun InfiniteWheelColumn(
    state: LazyListState,
    itemCount: Int,
    selectedItem: Int,
    alignment: Alignment.Horizontal
) {
    LazyColumn(
        state = state,
        // Hút về giữa nằm ngay trong cú vuốt (kể cả thả tay không có đà) -> không cần cuộn bù sau đó
        flingBehavior = rememberSnapFlingBehavior(state, SnapPosition.Center),
        modifier = Modifier
            .wrapContentWidth()
            .height(WheelViewportHeight),
        horizontalAlignment = alignment,
        // Đệm đúng nửa phần thừa -> mục ở vị trí cuộn nằm chính giữa khung
        contentPadding = PaddingValues(vertical = (WheelViewportHeight - WheelItemHeight) / 2)
    ) {
        items(count = Int.MAX_VALUE) { index ->
            val value = index % itemCount
            val isSelected = (value == selectedItem)

            TimePickerItem(
                text = "%02d".format(value),
                isSelected = isSelected
            )
        }
    }
}

/**
 * Khi bánh xe dừng: báo index tuyệt đối của mục ở tâm. Việc hút về giữa do [rememberSnapFlingBehavior] làm;
 * ở đây chỉ chỉnh nốt nếu một lần cuộn bằng code bị ngắt giữa chừng.
 *
 * Trước đây effect này dùng LaunchedEffect(isScrollInProgress) rồi tự animateScrollToItem: chính cú cuộn đó làm
 * khoá đổi -> effect bị huỷ giữa chừng, số dừng lệch khỏi dải chọn. Thu theo snapshotFlow thì không bị huỷ.
 * Toạ độ item tính từ sau contentPadding, nên tâm thật = (viewportStartOffset + viewportEndOffset) / 2.
 */
@Composable
private fun RememberInfiniteSnap(
    lazyListState: LazyListState,
    onItemSettled: (index: Int) -> Unit
) {
    val currentOnItemSettled by rememberUpdatedState(onItemSettled)
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.isScrollInProgress }
            .filter { inProgress -> !inProgress }
            .collect {
                val layoutInfo = lazyListState.layoutInfo
                val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closestItem = layoutInfo.visibleItemsInfo.minByOrNull {
                    abs((it.offset + it.size / 2) - center)
                } ?: return@collect
                val drift = closestItem.offset + closestItem.size / 2 - center
                if (drift != 0) lazyListState.animateScrollBy(drift.toFloat())
                currentOnItemSettled(closestItem.index)
            }
    }
}

@Composable
private fun TimePickerItem(
    text: String,
    isSelected: Boolean
) {
    Box(Modifier.height(WheelItemHeight), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = if (isSelected) 36.sp else 32.sp,
            color = if (isSelected) Calm.Foreground else Calm.MutedForeground,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
