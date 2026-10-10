package com.cong.smartalarm.feature.alarmedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.designsystem.component.CalmSwitch
import com.cong.smartalarm.core.designsystem.theme.Calm


/** Công tắc "Hàng ngày" + 7 chip chọn ngày lặp lại. */
@Composable
fun DaySelectorSection(
    daysOfWeek: Set<String>,
    onRepeatDailyChange: (Boolean) -> Unit,
    onDayToggle: (String) -> Unit
) {
    val isDaily = daysOfWeek.size == 7
    val dayOrder = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRepeatDailyChange(!isDaily) }
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hàng ngày",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Calm.Foreground
            )
            CalmSwitch(
                checked = isDaily,
                onCheckedChange = { onRepeatDailyChange(it) }
            )
        }

        // 7 chip chia đều bề ngang: 7 × 44dp cố định bị tràn trên máy hẹp nên chip "CN" bị bóp nhỏ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dayOrder.forEach { day ->
                CustomDayChip(
                    text = day,
                    isSelected = daysOfWeek.contains(day),
                    onClick = { onDayToggle(day) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomDayChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (isSelected) Calm.Primary else Calm.NeutralMist)
            .border(1.dp, if (isSelected) Calm.Primary else Calm.NeutralSoft, shape)
            .selectable(selected = isSelected, role = Role.Checkbox, onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Calm.Foreground,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
    }
}
