package com.example.myapplication.ui.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PermissionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            // Cấp quyền: Xanh đậm nền đen | Chưa cấp: Xám đậm
            containerColor = if (isGranted) Color(0xFF1B5E20).copy(alpha = 0.2f) else Color(0xFF212121)
        ),
        // Thêm viền để phân biệt rõ hơn trên nền đen
        border = BorderStroke(
            width = 1.dp,
            color = if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
        ),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                // Icon đổi màu theo trạng thái
                tint = if (isGranted) Color(0xFF81C784) else Color.White.copy(alpha = 0.6f)
            )
            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) Color(0xFF81C784) else Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            if (isGranted) {
                Icon(Icons.Default.CheckCircle, "Done", tint = Color(0xFF4CAF50))
            } else {
                // Thêm icon mũi tên hoặc cảnh báo nhỏ nếu chưa cấp quyền
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}