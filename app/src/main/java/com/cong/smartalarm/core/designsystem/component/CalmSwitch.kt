package com.cong.smartalarm.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cong.smartalarm.core.designsystem.theme.Calm

/**
 * Màu công tắc dùng chung: bật = rãnh đen, nút trắng; tắt = rãnh xám nhạt có viền,
 * nút xám (nút trắng trên rãnh nhạt gần như không thấy).
 */
@Composable
fun calmSwitchColors(): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = Color.Transparent,
    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
    uncheckedTrackColor = Calm.Muted,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

@Composable
fun CalmSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = calmSwitchColors()
    )
}
