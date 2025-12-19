package com.example.myapplication.ui.theme.alarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun QuickAlarmDialog(
    currentSoundTitle: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onSoundClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onSave: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismissRequest() },
        contentAlignment = Alignment.BottomCenter
        ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
            ) {
//            QuickAlarmContent(onDismissRequest, onSave)
            QuickAlarmContent(
                currentSoundTitle = currentSoundTitle,
                currentVolume = volume,
                onVolumeChange = onVolumeChange,
                onSoundClick = onSoundClick,
                onDismissRequest = onDismissRequest,
                onSave = onSave
            )
            }
        }
    }
}
@Composable
fun QuickAlarmContent(
    currentSoundTitle: String,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    onSoundClick: () -> Unit,

    // Các tham số dành cho logic báo thức nhanh
    onDismissRequest: () -> Unit,
    onSave: (Int) -> Unit
) {
    // Trạng thái cục bộ: Số phút thêm vào (Chỉ dùng trong Dialog này)
    var totalAddedMinutes by remember { mutableIntStateOf(0) }

    val displayHours = totalAddedMinutes / 60
    val displayMinutes = totalAddedMinutes % 60

    val targetTime = remember(totalAddedMinutes) {
        LocalTime.now().plusMinutes(totalAddedMinutes.toLong())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Báo thức nhanh",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- HIỂN THỊ THỜI GIAN ---
            QuickTimePickerSection(
                displayHours = displayHours,
                displayMinutes = displayMinutes,
                targetTime = targetTime,
                onAddMinutes = { totalAddedMinutes += it },
                onReset = { totalAddedMinutes = 0 }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- PHẦN CHỌN NHẠC (AlarmSoundSection Style) ---
            AlarmSoundSection(
                currentSoundTitle = currentSoundTitle,
                currentVolume = currentVolume,
                onVolumeChange = onVolumeChange,
                onSoundClick = onSoundClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- NÚT LƯU ---
            Button(
                onClick = { onSave(totalAddedMinutes) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("BẮT ĐẦU", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickTimePickerSection(
    displayHours: Int,
    displayMinutes: Int,
    targetTime: String,
    onAddMinutes: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("+", color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, modifier = Modifier.padding(bottom = 8.dp))
            Spacer(modifier = Modifier.width(8.dp))
            if (displayHours > 0) {
                Text("$displayHours", color = MaterialTheme.colorScheme.onSurface, fontSize = 64.sp, fontWeight = FontWeight.Light)
                Text("h", color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, modifier = Modifier.padding(top = 20.dp, end = 12.dp))
            }
            Text("$displayMinutes", color = MaterialTheme.colorScheme.onSurface, fontSize = 64.sp, fontWeight = FontWeight.Light)
            Text("m", color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, modifier = Modifier.padding(top = 20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = null,tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
            }
        }
        Text("Đổ chuông lúc $targetTime", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(20.dp))

        val options = listOf(1, 5, 10, 30, 60, 120)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.chunked(3).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { min ->
                        Button(
                            onClick = { onAddMinutes(min) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (min >= 60) "${min/60}h" else "${min}m", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}