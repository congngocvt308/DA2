package com.example.myapplication.ui.theme.qrcode

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.QRCodeEntity

/**
 * Dialog quản lý QR/Barcode - Xem, thêm, xóa các mã đã lưu
 */
@Composable
fun QRCodeManagementDialog(
    onDismiss: () -> Unit,
    onScanCamera: () -> Unit,
    scannedCodeFromCamera: Pair<String, String>? = null,
    onClearScannedCode: () -> Unit = {},
    viewModel: QRCodeViewModel = viewModel()
) {
    val context = LocalContext.current
    val qrCodes by viewModel.allQRCodes.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
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
                    color = MaterialTheme.colorScheme.tertiary
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
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Chưa có mã nào được lưu",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Thêm mã QR hoặc Barcode để sử dụng\nkhi tắt báo thức",
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
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
                        items(qrCodes) { qrCode ->
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
        AlertDialog(
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
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
                    MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (enabled) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QRCodeItem(
    qrCode: QRCodeEntity,
    onDelete: () -> Unit
) {
    Card(
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
                            Color(0xFF6750A4).copy(alpha = 0.2f) 
                        else 
                            Color(0xFF7D5260).copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (qrCode.codeType == "QR") Icons.Default.QrCode2 else Icons.Default.ViewWeek,
                    contentDescription = null,
                    tint = if (qrCode.codeType == "QR") Color(0xFF6750A4) else Color(0xFF7D5260),
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
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = qrCode.codeValue.take(30) + if (qrCode.codeValue.length > 30) "..." else "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
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

/**
 * Dialog để thêm mã mới (nhập tay hoặc sau khi quét)
 */
@Composable
fun AddQRCodeDialog(
    initialCode: String,
    initialType: String,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, type: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf(initialCode) }
    var selectedType by remember { mutableStateOf(initialType) }
    
    val isFromScan = initialCode.isNotEmpty()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (isFromScan) "Lưu mã đã quét" else "Thêm mã mới",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Tên mã
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên mã (VD: Mã tủ lạnh)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Giá trị mã
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Giá trị mã") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFromScan, // Không cho sửa nếu đã quét
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Chọn loại mã (chỉ khi nhập tay)
                if (!isFromScan) {
                    Text(
                        text = "Loại mã",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TypeSelectionChip(
                            modifier = Modifier.weight(1f),
                            text = "QR Code",
                            isSelected = selectedType == "QR",
                            onClick = { selectedType = "QR" }
                        )
                        TypeSelectionChip(
                            modifier = Modifier.weight(1f),
                            text = "Barcode",
                            isSelected = selectedType == "BARCODE",
                            onClick = { selectedType = "BARCODE" }
                        )
                    }
                } else {
                    // Hiển thị loại mã đã phát hiện
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Loại mã phát hiện: ",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (selectedType == "QR") "QR Code" else "Barcode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = { onSave(name, code, selectedType) },
                        enabled = name.isNotBlank() && code.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeSelectionChip(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

