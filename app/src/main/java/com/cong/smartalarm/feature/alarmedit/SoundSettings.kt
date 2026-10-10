package com.cong.smartalarm.feature.alarmedit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.designsystem.component.CalmSlider
import com.cong.smartalarm.core.designsystem.theme.Calm


/** Thẻ "Âm thanh báo thức": chọn nhạc chuông + thanh âm lượng. */
@Composable
internal fun SoundSettingsSection(
    currentSoundTitle: String,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    onSoundClick: () -> Unit
) {
    SettingsSection {
        SettingsSectionHeader(title = "Âm thanh báo thức")
        Spacer(modifier = Modifier.height(12.dp))

        // Dòng tên bài hát
        SettingsRow(onClick = onSoundClick) {
            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Calm.Primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = currentSoundTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Calm.Foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Calm.MutedForeground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dòng Slider Volume
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Âm lượng", tint = Calm.MutedForeground)
            Spacer(modifier = Modifier.width(12.dp))
            CalmSlider(
                value = currentVolume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun VolumeSliderRow(volume: Float, onVolumeChange: (Float) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Âm lượng",
            tint = MaterialTheme.colorScheme.onSurface
        )
        CalmSlider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SoundSelectionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Mở danh sách nhạc */ }
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "TOKUSOU SENTAI DEKAR...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
