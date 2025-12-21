package com.example.myapplication.ui.theme.qrcode

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Màn hình quét QR/Barcode để tắt báo thức
 */
@Composable
fun QRDismissScreen(
    alarmId: Int,
    onBack: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).appDao() }
    
    var isScanning by remember { mutableStateOf(true) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var requiredCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Load các mã QR yêu cầu cho báo thức này
    LaunchedEffect(alarmId) {
        val qrCodes = dao.getQRCodesForAlarmOnce(alarmId)
        requiredCodes = qrCodes.map { it.name }
    }
    
    if (isScanning) {
        QRCodeScannerScreen(
            scannerTitle = "Quét để tắt báo thức",
            scannerHint = "Quét mã QR/Barcode đã cài đặt",
            onCodeScanned = { code, type ->
                isScanning = false
                
                scope.launch {
                    val isValid = dao.isQRCodeValidForAlarm(alarmId, code)
                    
                    if (isValid) {
                        scanResult = ScanResult.Success(code)
                        delay(1500)
                        onDismissSuccess()
                    } else {
                        scanResult = ScanResult.Invalid(code)
                    }
                }
            },
            onClose = onBack
        )
    } else {
        // Hiển thị kết quả quét
        ScanResultScreen(
            result = scanResult,
            requiredCodes = requiredCodes,
            onRetry = {
                isScanning = true
                scanResult = null
            },
            onBack = onBack
        )
    }
}

@Composable
private fun ScanResultScreen(
    result: ScanResult?,
    requiredCodes: List<String>,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White
            )
        }
        
        when (result) {
            is ScanResult.Success -> {
                SuccessContent()
            }
            is ScanResult.Invalid -> {
                InvalidContent(
                    scannedCode = result.code,
                    requiredCodes = requiredCodes,
                    onRetry = onRetry
                )
            }
            null -> {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun SuccessContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Mã hợp lệ!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Đang tắt báo thức...",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun InvalidContent(
    scannedCode: String,
    requiredCodes: List<String>,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE50043)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Mã không hợp lệ!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE50043)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Mã bạn quét không nằm trong\ndanh sách mã đã cài đặt",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        if (requiredCodes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Các mã đã cài đặt:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    requiredCodes.forEach { codeName ->
                        Text(
                            text = "• $codeName",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Quét lại",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

private sealed class ScanResult {
    data class Success(val code: String) : ScanResult()
    data class Invalid(val code: String) : ScanResult()
}

