package com.example.myapplication.ui.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.utils.PremiumManager
import kotlinx.coroutines.delay

/**
 * Dialog thanh toán giả (Fake Purchase Dialog)
 * 
 * Hiển thị giao diện mua premium với animation đẹp mắt.
 * Khi nhấn "Mua ngay", sẽ giả lập quá trình thanh toán và lưu vào SharedPreferences.
 */
@Composable
fun PremiumPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    val context = LocalContext.current
    val premiumManager = remember { PremiumManager.getInstance(context) }
    
    var purchaseState by remember { mutableStateOf(PurchaseState.INITIAL) }
    
    LaunchedEffect(purchaseState) {
        if (purchaseState == PurchaseState.PROCESSING) {
            // Giả lập thời gian xử lý thanh toán
            delay(1500)
            
            val success = premiumManager.processFakePurchase()
            purchaseState = if (success) PurchaseState.SUCCESS else PurchaseState.FAILED
            
            if (success) {
                delay(1000)
                onPurchaseSuccess()
            }
        }
    }
    
    Dialog(
        onDismissRequest = { 
            if (purchaseState != PurchaseState.PROCESSING) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = purchaseState != PurchaseState.PROCESSING,
            dismissOnClickOutside = purchaseState != PurchaseState.PROCESSING
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            when (purchaseState) {
                PurchaseState.INITIAL -> InitialPurchaseContent(
                    onDismiss = onDismiss,
                    onPurchaseClick = { purchaseState = PurchaseState.PROCESSING }
                )
                PurchaseState.PROCESSING -> ProcessingContent()
                PurchaseState.SUCCESS -> SuccessContent()
                PurchaseState.FAILED -> FailedContent(
                    onRetry = { purchaseState = PurchaseState.PROCESSING },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun InitialPurchaseContent(
    onDismiss: () -> Unit,
    onPurchaseClick: () -> Unit
) {
    val goldColor = Color(0xFFFFD700)
    val goldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFE066), goldColor, Color(0xFFCC9900))
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Đóng",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDismiss() }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Premium icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(goldGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Nâng cấp Premium",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Mở khóa tất cả tính năng nâng cao",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Features list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumFeatureItem("Nhiệm vụ giải toán")
            PremiumFeatureItem("Nhiệm vụ ghi nhớ hình ảnh")
            PremiumFeatureItem("Nhiệm vụ quét mã QR/Barcode")
            PremiumFeatureItem("Không giới hạn số lượng báo thức")
            PremiumFeatureItem("Mua một lần, dùng mãi mãi")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Price
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "29.000",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = goldColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "VNĐ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = goldColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Text(
            text = "Thanh toán một lần",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Purchase button
        Button(
            onClick = onPurchaseClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = goldColor
            )
        ) {
            Text(
                text = "Mua ngay",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Note for testing
        Text(
            text = "⚠️ Đây là thanh toán GIẢ dành cho testing",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProcessingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Color(0xFFFFD700),
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Đang xử lý thanh toán...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Vui lòng đợi trong giây lát",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun SuccessContent() {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "success_scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Thành công!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Bạn đã mở khóa tất cả tính năng Premium",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FailedContent(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Thanh toán thất bại",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Đã có lỗi xảy ra. Vui lòng thử lại.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
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
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Thử lại")
            }
        }
    }
}

private enum class PurchaseState {
    INITIAL,
    PROCESSING,
    SUCCESS,
    FAILED
}

