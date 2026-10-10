package com.cong.smartalarm.ui.theme.topic


import com.cong.smartalarm.ui.theme.components.GlassCard
import com.cong.smartalarm.ui.theme.components.GlassButton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun QuickTopicDialog(
    initialName: String,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(enabled = false) {}
            ) {
                QuickTopicContent(
                    initialName,
                    onDismissRequest,
                    onSave
                )
            }
        }
    }
}

@Composable
fun QuickTopicContent(
    initialName: String,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit
) {
    var topicName by remember { mutableStateOf(initialName) }

    GlassCard(
        shape = RoundedCornerShape(28.dp),
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
                Text(text = if (topicName.isEmpty()) "Thêm chủ đề mới" else "Đổi tên chủ đề", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = MaterialTheme.colorScheme.onSurface,
                    // Vùng chạm 48dp
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onDismissRequest() }
                        .padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = topicName,
                onValueChange = { topicName = it},
                label = { Text("Tên chủ đề", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            GlassButton(
                onClick = {
                    if (topicName.isNotBlank()) {
                        onSave(topicName.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(if (topicName.isEmpty()) "Tạo chủ đề" else "Lưu thay đổi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}