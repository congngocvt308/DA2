package com.cong.smartalarm.feature.qr

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.core.designsystem.theme.Calm

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Dialog để chọn QR codes cho một báo thức cụ thể
 * Tối đa 3 mã cho mỗi báo thức
 */
@Composable
fun QRCodeSelectionDialog(
    alarmId: Int,
    initialSelectedIds: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit,
    onManageQRCodes: () -> Unit,
    viewModel: QRCodeViewModel = viewModel(factory = QRCodeViewModel.Factory)
) {
    val context = LocalContext.current
    val allQRCodes by viewModel.allQRCodes.collectAsStateWithLifecycle()
    
    var selectedIds by remember { mutableStateOf(initialSelectedIds.toSet()) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CalmCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chọn mã QR/Barcode",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Subtitle
                Text(
                    text = "Đã chọn: ${selectedIds.size}/${QRCodeViewModel.MAX_QR_PER_ALARM}",
                    fontSize = 14.sp,
                    color = if (selectedIds.size >= QRCodeViewModel.MAX_QR_PER_ALARM) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (allQRCodes.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.QrCode2,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Calm.Subtle
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có mã nào được lưu",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Thêm mã QR/Barcode để sử dụng\nkhi tắt báo thức",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            CalmButton(
                                onClick = onManageQRCodes,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thêm mã")
                            }
                        }
                    }
                } else {
                    // List of QR codes
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(allQRCodes, key = { it.qrId }) { qrCode ->
                            val isSelected = selectedIds.contains(qrCode.qrId)
                            val canSelect = selectedIds.size < QRCodeViewModel.MAX_QR_PER_ALARM || isSelected
                            
                            SelectableQRCodeItem(
                                qrCode = qrCode,
                                isSelected = isSelected,
                                canSelect = canSelect,
                                onToggle = {
                                    if (isSelected) {
                                        selectedIds = selectedIds - qrCode.qrId
                                    } else if (canSelect) {
                                        selectedIds = selectedIds + qrCode.qrId
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Chỉ có thể chọn tối đa ${QRCodeViewModel.MAX_QR_PER_ALARM} mã",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                        
                        // Button to manage QR codes
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = onManageQRCodes,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Quản lý mã QR/Barcode")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info card
                if (selectedIds.isNotEmpty()) {
                    CalmCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Calm.Raised
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quét bất kỳ mã nào đã chọn để tắt báo thức",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Hủy")
                    }
                    
                    CalmButton(
                        onClick = { onConfirm(selectedIds.toList()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Xác nhận")
                    }
                }
            }
        }
    }
}
