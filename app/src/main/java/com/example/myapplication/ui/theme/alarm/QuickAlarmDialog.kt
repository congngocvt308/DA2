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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun QuickAlarmDialog(
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
            QuickAlarmContent(onDismissRequest, onSave)
            }
        }
    }
}

@Composable
fun QuickAlarmContent(
    onDismissRequest: () -> Unit,
    onSave: (Int) -> Unit
) {
    var totalAddedMinutes by remember { mutableIntStateOf(0) }
    var volume by remember { mutableFloatStateOf(0.7f) }
    val displayHours = totalAddedMinutes / 60
    val displayMinutes = totalAddedMinutes % 60
    val targetTime = remember(totalAddedMinutes) {
        val time = LocalTime.now().plusMinutes(totalAddedMinutes.toLong())
        time.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Báo thức nhanh", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { onDismissRequest() }
                        .size(35.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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

                IconButton(onClick = { totalAddedMinutes = 0 }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(30.dp))
                }
            }

            Text(
                text = "Đổ chuông lúc $targetTime",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            val timeOptions = listOf(
                Pair(1, "1 phút"),
                Pair(5, "5 phút"),
                Pair(10, "10 phút"),
                Pair(30, "30 phút"),
                Pair(60, "1 giờ"),
                Pair(120, "2 giờ")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                timeOptions.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { (minutes, label) ->
                            Button(
                                onClick = { totalAddedMinutes += minutes },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(50.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text = label, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Mở danh sách nhạc */ },
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

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.onSurface)

                Spacer(modifier = Modifier.width(12.dp))

                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onSurface,
                        activeTrackColor = MaterialTheme.colorScheme.onSurface,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSave(totalAddedMinutes) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Preview
@Composable
fun PreviewQuickDialog() {
    QuickAlarmContent(onDismissRequest = {}, onSave = {})
}