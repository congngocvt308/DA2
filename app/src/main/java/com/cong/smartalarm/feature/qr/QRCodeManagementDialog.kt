package com.cong.smartalarm.feature.qr

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.component.CalmAlertDialog
import com.cong.smartalarm.core.designsystem.theme.Calm

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.core.database.entity.QRCodeEntity

/**
 * Dialog quản lý QR/Barcode - Xem, thêm, xóa các mã đã lưu
 */
@Composable
fun QRCodeManagementDialog(
    onDismiss: () -> Unit,
    onScanCamera: () -> Unit,
    scannedCodeFromCamera: Pair<String, String>? = null,
    onClearScannedCode: () -> Unit = {},
    viewModel: QRCodeViewModel = viewModel(factory = QRCodeViewModel.Factory)
) {
    val context = LocalContext.current
    val qrCodes by viewModel.allQRCodes.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<QRCodeEntity?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Khi có code từ camera, tự động hiển thị dialog thêm
    LaunchedEffect(scannedCodeFromCamera) {
        if (scannedCodeFromCamera != null) {
            showAddDialog = true
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.scanFromImage(
                uri = selectedUri,
                onResult = { code, type ->
                    showAddDialog = true
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onScanCamera()
        } else {
            Toast.makeText(context, "Cần quyền camera để quét mã", Toast.LENGTH_SHORT).show()
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CalmCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
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
                        text = "Quản lý mã QR/Barcode",
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Info text
                Text(
                    text = "Lưu tối đa ${QRCodeViewModel.MAX_QR_CODES} mã • Đã lưu: ${qrCodes.size}/${QRCodeViewModel.MAX_QR_CODES}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quét từ camera
                    AddMethodButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CameraAlt,
                        text = "Quét camera",
                        enabled = qrCodes.size < QRCodeViewModel.MAX_QR_CODES,
                        onClick = {
                            val permission = Manifest.permission.CAMERA
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                onScanCamera()
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                        }
                    )
                    
                    // Chọn từ thư viện
                    AddMethodButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Image,
                        text = "Từ ảnh",
                        enabled = qrCodes.size < QRCodeViewModel.MAX_QR_CODES,
                        onClick = { imagePickerLauncher.launch("image/*") }
                    )
                    
                    // Nhập tay
                    AddMethodButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Edit,
                        text = "Nhập tay",
                        enabled = qrCodes.size < QRCodeViewModel.MAX_QR_CODES,
                        onClick = { 
                            viewModel.clearScannedCode()
                            showAddDialog = true 
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Danh sách QR codes đã lưu
                Text(
                    text = "Mã đã lưu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (qrCodes.isEmpty()) {
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
                                tint = Calm.NeutralSoft
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có mã nào được lưu",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Thêm mã QR hoặc Barcode để sử dụng\nkhi tắt báo thức",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(qrCodes, key = { it.qrId }) { qrCode ->
                            QRCodeItem(
                                qrCode = qrCode,
                                onDelete = { showDeleteConfirm = qrCode }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialog thêm mã mới
    if (showAddDialog) {
        // Ưu tiên code từ camera, sau đó mới đến code từ image picker
        val codeToUse = scannedCodeFromCamera?.first ?: uiState.scannedCode ?: ""
        val typeToUse = scannedCodeFromCamera?.second ?: uiState.scannedType ?: "BARCODE"
        
        AddQRCodeDialog(
            initialCode = codeToUse,
            initialType = typeToUse,
            onDismiss = { 
                showAddDialog = false
                viewModel.clearScannedCode()
                onClearScannedCode() // Clear camera scanned code
            },
            onSave = { name, code, type ->
                viewModel.saveQRCode(
                    name = name,
                    codeValue = code,
                    codeType = type,
                    onSuccess = {
                        showAddDialog = false
                        viewModel.clearScannedCode()
                        onClearScannedCode() // Clear camera scanned code
                        Toast.makeText(context, "Đã lưu mã thành công", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
    
    // Dialog xác nhận xóa
    showDeleteConfirm?.let { qrCode ->
        CalmAlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Xóa mã?") },
            text = { Text("Bạn có chắc muốn xóa \"${qrCode.name}\"? Mã này sẽ bị hủy liên kết khỏi tất cả báo thức.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQRCode(qrCode) {
                            Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteConfirm = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun AddMethodButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    CalmCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                Calm.Muted
        ),
        onClick = { if (enabled) onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (enabled) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
