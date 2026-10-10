package com.cong.smartalarm.feature.qr

import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.database.entity.QRCodeEntity

/** Một mã đã lưu trong dialog quản lý, có nút xóa. */
@Composable
internal fun QRCodeItem(
    qrCode: QRCodeEntity,
    onDelete: () -> Unit
) {
    CalmCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon theo loại mã
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (qrCode.codeType == "QR") 
                            Calm.Raised 
                        else 
                            Calm.WellnessMist
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (qrCode.codeType == "QR") Icons.Default.QrCode2 else Icons.Default.ViewWeek,
                    contentDescription = null,
                    tint = if (qrCode.codeType == "QR") Calm.Primary else Calm.WellnessText,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = qrCode.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (qrCode.codeType == "QR") "Mã QR" else "Barcode",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = qrCode.codeValue.take(30) + if (qrCode.codeValue.length > 30) "..." else "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
