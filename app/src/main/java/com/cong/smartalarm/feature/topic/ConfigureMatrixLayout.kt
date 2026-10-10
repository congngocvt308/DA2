package com.cong.smartalarm.feature.topic

import com.cong.smartalarm.core.designsystem.component.CalmSlider
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.core.designsystem.theme.Calm
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.feature.topic.model.AiMatrixConfig

/** Bước cấu hình ma trận độ khó (chủ đề, preset, slider) trước khi tạo câu hỏi. */
@Composable
internal fun ConfigureMatrixLayout(
    config: AiMatrixConfig,
    onTopicChange: (String) -> Unit,
    onPresetSelect: (Boolean) -> Unit,
    onSliderChange: (Float, Float, Float) -> Unit,
    onGenerateClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Chủ đề nhận diện được:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = config.suggestedTopic,
            onValueChange = onTopicChange,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Cấu hình số lượng câu hỏi:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))

        // Chip đang chọn có dấu tích, không chỉ đổi màu
        val easyPreset = config.easyCount == 3f && config.midCount == 1f && config.hardCount == 0f
        val hardPreset = config.easyCount == 1f && config.midCount == 3f && config.hardCount == 2f
        val chipColors = FilterChipDefaults.filterChipColors(
            containerColor = Calm.Surface,
            labelColor = Calm.MutedForeground,
            selectedContainerColor = Calm.Raised,
            selectedLabelColor = Calm.Foreground,
            selectedLeadingIconColor = Calm.Primary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = easyPreset,
                onClick = { onPresetSelect(false) },
                label = { Text("Chế độ 'Dễ thở' (3-1-0)") },
                leadingIcon = if (easyPreset) {
                    { Icon(Icons.Filled.Check, contentDescription = "Đang chọn", modifier = Modifier.size(18.dp)) }
                } else null,
                colors = chipColors,
                shape = RoundedCornerShape(12.dp)
            )
            FilterChip(
                selected = hardPreset,
                onClick = { onPresetSelect(true) },
                label = { Text("Chế độ 'Try-hard' (1-3-2)") },
                leadingIcon = if (hardPreset) {
                    { Icon(Icons.Filled.Check, contentDescription = "Đang chọn", modifier = Modifier.size(18.dp)) }
                } else null,
                colors = chipColors,
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DifficultySliderItem(label = "Dễ (ez)", value = config.easyCount) { onSliderChange(it, config.midCount, config.hardCount) }
        DifficultySliderItem(label = "Trung bình (mid)", value = config.midCount) { onSliderChange(config.easyCount, it, config.hardCount) }
        DifficultySliderItem(label = "Khó (hard)", value = config.hardCount) { onSliderChange(config.easyCount, config.midCount, it) }

        Spacer(modifier = Modifier.height(16.dp))

        val totalQuestions = (config.easyCount + config.midCount + config.hardCount).toInt()
        CalmButton(
            onClick = onGenerateClick,
            enabled = totalQuestions > 0,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Bắt đầu tạo $totalQuestions câu hỏi bằng AI", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun DifficultySliderItem(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "${value.toInt()} câu", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        CalmSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9
        )
    }
}
