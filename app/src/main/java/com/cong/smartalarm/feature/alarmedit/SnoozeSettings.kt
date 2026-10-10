package com.cong.smartalarm.feature.alarmedit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.component.CalmSwitch
import com.cong.smartalarm.core.designsystem.theme.Calm


/** Thẻ "Báo lại": công tắc + khoảng thời gian (hiện khi bật). */
@Composable
fun SnoozeSettingsSection(
    isSnoozeEnabled: Boolean,
    snoozeDuration: Int,
    onSnoozeToggle: (Boolean) -> Unit,
    onDurationClick: () -> Unit
) {
    SettingsSection {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Báo lại",
                color = Calm.Foreground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            CalmSwitch(
                checked = isSnoozeEnabled,
                onCheckedChange = onSnoozeToggle
            )
        }

        AnimatedVisibility(
            visible = isSnoozeEnabled,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                SettingsRow(onClick = onDurationClick) {
                    Text(
                        text = "Khoảng thời gian",
                        color = Calm.Foreground,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$snoozeDuration phút",
                        color = Calm.Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Calm.Primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}


/** Hộp thoại chọn số phút báo lại. */
@Composable
fun SnoozeDurationDialog(
    currentDuration: Int,
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    val options = listOf(5, 10, 15, 20, 25, 30)

    Dialog(onDismissRequest = onDismiss) {
        CalmCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Thời gian báo lại",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(options) { duration ->
                        val isSelected = (duration == currentDuration)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDurationSelected(duration) }
                                .background(if (isSelected) Calm.Raised else Color.Transparent)
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$duration phút",
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
