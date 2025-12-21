package com.example.myapplication.ui.theme.alarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.AlarmData

@Composable
fun AlarmCard(
    alarmData: AlarmData,
    onToggle: (Boolean) -> Unit,
    onCardClick: () -> Unit,
    onDelete:() -> Unit){
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alarmData.days,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.Companion.size(24.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Xóa báo thức",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.Companion.size(24.dp)
                    )
                }

            }
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alarmData.time,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Companion.ExtraBold
                )
                Switch(
                    checked = alarmData.isEnabled,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.secondary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.sun),
                    contentDescription = null,
                    modifier = Modifier.Companion.size(16.dp)
                )
                Spacer(Modifier.Companion.width(4.dp))
                alarmData.label?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmSoundSection(
    currentSoundTitle: String,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    onSoundClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)), // Màu nền tối
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Âm thanh báo thức",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dòng tên bài hát
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSoundClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = currentSoundTitle,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dòng Slider Volume
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Slider(
                    value = currentVolume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFFE50043), // Màu đỏ của app
                        inactiveTrackColor = Color.Gray
                    )
                )
            }
        }
    }
}