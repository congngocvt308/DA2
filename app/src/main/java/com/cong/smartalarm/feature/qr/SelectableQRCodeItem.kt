package com.cong.smartalarm.feature.qr

import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

/** Mã QR/Barcode có ô tích để chọn cho báo thức. */
@Composable
internal fun SelectableQRCodeItem(
    qrCode: QRCodeEntity,
    isSelected: Boolean,
    canSelect: Boolean,
    onToggle: () -> Unit
) {
    CalmCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Calm.NeutralMist
            else
                Calm.Surface
        ),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else if (canSelect)
                            Calm.Surface
                        else
                            Calm.Muted
                    )
                    .border(
                        2.dp,
                        if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else if (canSelect)
                            MaterialTheme.colorScheme.outline
                        else
                            MaterialTheme.colorScheme.outlineVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Đã chọn",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Icon theo loại mã
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (qrCode.codeType == "QR")
                            Calm.NeutralMist
                        else
                            Calm.WellnessMist
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (qrCode.codeType == "QR") Icons.Default.QrCode2 else Icons.Default.ViewWeek,
                    contentDescription = null,
                    tint = if (qrCode.codeType == "QR") Calm.Primary else Calm.WellnessText,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = qrCode.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (canSelect || isSelected)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (qrCode.codeType == "QR") "QR Code" else "Barcode",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
