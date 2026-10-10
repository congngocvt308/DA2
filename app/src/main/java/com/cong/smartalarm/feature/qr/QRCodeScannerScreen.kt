package com.cong.smartalarm.feature.qr
import com.cong.smartalarm.core.designsystem.component.LocalGlassBackdrop
import androidx.compose.runtime.CompositionLocalProvider

import com.cong.smartalarm.core.designsystem.component.GlassCard
import com.cong.smartalarm.core.designsystem.component.rememberReducedMotion
import com.cong.smartalarm.core.designsystem.theme.Calm

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cong.smartalarm.core.camera.BarcodeScannerProvider
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Màn hình quét mã QR/Barcode bằng camera
 */
@Composable
fun QRCodeScannerScreen(
    onCodeScanned: (code: String, type: String) -> Unit,
    onClose: () -> Unit,
    scannerTitle: String = "Quét mã QR/Barcode",
    scannerHint: String = "Đặt mã vào khung hình để quét"
) {
    // Màn có camera/ảnh phía sau: kính không khúc xạ nền aurora (sẽ lệch), dùng kính "khói".
    CompositionLocalProvider(LocalGlassBackdrop provides null) {
        QRCodeScannerContent(onCodeScanned, onClose, scannerTitle, scannerHint)
    }
}

@Composable
private fun QRCodeScannerContent(
    onCodeScanned: (code: String, type: String) -> Unit,
    onClose: () -> Unit,
    scannerTitle: String,
    scannerHint: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isFlashOn by remember { mutableStateOf(false) }
    val hasScanned = remember { AtomicBoolean(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    val barcodeScanner = remember { BarcodeScannerProvider.scanner }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            hasScanned.set(false)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (hasScanned.get()) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        @androidx.camera.core.ExperimentalGetImage
                        val mediaImage = imageProxy.image
                        if (mediaImage == null) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                if (barcodes.isNotEmpty() && hasScanned.compareAndSet(false, true)) {
                                    val barcode = barcodes.first()
                                    val codeValue = barcode.rawValue ?: return@addOnSuccessListener
                                    onCodeScanned(codeValue, getCodeType(barcode.format))
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                    
                    try {
                        provider.unbindAll()
                        camera = provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            }
        )
        
        // Overlay với khung quét
        val colorScheme = MaterialTheme.colorScheme
        ScannerOverlay(colorScheme = colorScheme)
        
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Calm.Surface)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Calm.Foreground
                )
            }
            
            // Flash toggle
            IconButton(
                onClick = {
                    camera?.cameraControl?.enableTorch(!isFlashOn)
                    isFlashOn = !isFlashOn
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isFlashOn) Calm.WarmMist else Calm.Surface)
            ) {
                Icon(
                    if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Đèn flash",
                    tint = if (isFlashOn) Calm.Warm else Calm.Foreground
                )
            }
        }
        
        // Title and hint (nằm trên lớp phủ tối -> chữ trắng)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = scannerTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = scannerHint,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
        
        // Bottom instruction
        GlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Calm.Surface
            )
        ) {
            Text(
                text = "Hỗ trợ QR Code và tất cả loại Barcode phổ biến",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScannerOverlay(colorScheme: androidx.compose.material3.ColorScheme) {
    // Vạch quét trôi chậm, êm; đứng yên giữa khung khi giảm chuyển động
    val reducedMotion = rememberReducedMotion()
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_line")
    val scanLineAnim by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )
    val scanLineY = if (reducedMotion) 0.5f else scanLineAnim
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Kích thước khung quét
        val frameSize = minOf(canvasWidth, canvasHeight) * 0.7f
        val frameLeft = (canvasWidth - frameSize) / 2
        val frameTop = (canvasHeight - frameSize) / 2
        val frameRight = frameLeft + frameSize
        val frameBottom = frameTop + frameSize
        
        // Vẽ lớp phủ tối ngoài khung
        val overlayPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    Rect(frameLeft, frameTop, frameRight, frameBottom),
                    CornerRadius(24.dp.toPx())
                )
            )
        }

        drawPath(
            path = overlayPath,
            color = Color(0xFF09090B).copy(alpha = 0.7f)
        )
        
        // Vẽ viền khung
        drawRoundRect(
            color = Color.White.copy(alpha = 0.6f),
            topLeft = Offset(frameLeft, frameTop),
            size = androidx.compose.ui.geometry.Size(frameSize, frameSize),
            cornerRadius = CornerRadius(24.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Vẽ góc highlight
        val cornerLength = 40.dp.toPx()
        val cornerStroke = 6.dp.toPx()
        val highlightColor = Calm.LavenderSoft
        
        // Top-left corner
        drawLine(
            color = highlightColor,
            start = Offset(frameLeft, frameTop + cornerLength),
            end = Offset(frameLeft, frameTop),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = highlightColor,
            start = Offset(frameLeft, frameTop),
            end = Offset(frameLeft + cornerLength, frameTop),
            strokeWidth = cornerStroke
        )
        
        // Top-right corner
        drawLine(
            color = highlightColor,
            start = Offset(frameRight - cornerLength, frameTop),
            end = Offset(frameRight, frameTop),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = highlightColor,
            start = Offset(frameRight, frameTop),
            end = Offset(frameRight, frameTop + cornerLength),
            strokeWidth = cornerStroke
        )
        
        // Bottom-left corner
        drawLine(
            color = highlightColor,
            start = Offset(frameLeft, frameBottom - cornerLength),
            end = Offset(frameLeft, frameBottom),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = highlightColor,
            start = Offset(frameLeft, frameBottom),
            end = Offset(frameLeft + cornerLength, frameBottom),
            strokeWidth = cornerStroke
        )
        
        // Bottom-right corner
        drawLine(
            color = highlightColor,
            start = Offset(frameRight - cornerLength, frameBottom),
            end = Offset(frameRight, frameBottom),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = highlightColor,
            start = Offset(frameRight, frameBottom - cornerLength),
            end = Offset(frameRight, frameBottom),
            strokeWidth = cornerStroke
        )
        
        // Scanning line animation
        val scanY = frameTop + (frameSize * scanLineY)
        drawLine(
            color = highlightColor.copy(alpha = 0.8f),
            start = Offset(frameLeft + 20.dp.toPx(), scanY),
            end = Offset(frameRight - 20.dp.toPx(), scanY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun getCodeType(format: Int): String {
    return when (format) {
        Barcode.FORMAT_QR_CODE -> "QR"
        Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
        Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
        Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39,
        Barcode.FORMAT_CODE_93, Barcode.FORMAT_CODABAR,
        Barcode.FORMAT_ITF -> "BARCODE"
        else -> "OTHER"
    }
}

