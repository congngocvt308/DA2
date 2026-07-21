package com.example.myapplication.ui.theme.facedetection

import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.data.analyzer.FaceMissionAnalyzer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

@Composable
fun CustomPaintProgressBar(
    progress: Float,
    @DrawableRes bgResId: Int,
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF534878),
    borderWidth: Dp = 4.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Background nét cọ - Mở rộng full chiều rộng Box và phóng scale để rộng bao trùm hoàn toàn
        Image(
            painter = painterResource(id = bgResId),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleX = 1.05f, scaleY = 1.05f) // Phóng to nhẹ nền cọ sang 2 bên và trên dưới
        )

        // 2. Progress Bar
        Canvas(
            modifier = Modifier
                .offset(y = 2.dp)
                .fillMaxWidth(0.90f)
                .height(32.dp)
        ) {
            val strokePx = borderWidth.toPx()

            val innerLeft = strokePx
            val innerTop = strokePx
            val innerWidth = size.width - (strokePx * 2)
            val innerHeight = size.height - (strokePx * 2)

            val innerRadius = innerHeight / 2

            val rainbowBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF1744),
                    Color(0xFFFF9100),
                    Color(0xFFFFEA00),
                    Color(0xFF00E676),
                    Color(0xFF00E5FF)
                ),
                start = Offset(innerLeft, 0f),
                end = Offset(innerLeft + innerWidth, 0f)
            )

            if (innerWidth > 0f && innerHeight > 0f) {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.5f),
                    topLeft = Offset(innerLeft, innerTop),
                    size = Size(innerWidth, innerHeight),
                    cornerRadius = CornerRadius(innerRadius, innerRadius)
                )

                val currentProgressWidth = innerWidth * progress.coerceIn(0f, 1f)
                if (currentProgressWidth > 0f) {
                    drawRoundRect(
                        brush = rainbowBrush,
                        topLeft = Offset(innerLeft, innerTop),
                        size = Size(currentProgressWidth, innerHeight),
                        cornerRadius = CornerRadius(innerRadius, innerRadius)
                    )
                }
            }

            val borderLeft = strokePx / 2
            val borderTop = strokePx / 2
            val borderWidthSize = size.width - strokePx
            val borderHeightSize = size.height - strokePx
            val borderRadius = borderHeightSize / 2

            drawRoundRect(
                color = borderColor,
                topLeft = Offset(borderLeft, borderTop),
                size = Size(borderWidthSize, borderHeightSize),
                cornerRadius = CornerRadius(borderRadius, borderRadius),
                style = Stroke(width = strokePx)
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun FaceMissionScreen(viewModel: FaceMissionViewModel, onLuongAComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var previewWidth by remember { mutableStateOf(0f) }
    var previewHeight by remember { mutableStateOf(0f) }

    // Đồng hồ thời gian thực
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("EEEE, d 'tháng' M", Locale.getDefault())
        while (true) {
            val now = Date()
            timeString = timeFormatter.format(now)
            dateString = dateFormatter.format(now)
            delay(1000L)
        }
    }

    val warningOverlayColor by animateColorAsState(
        targetValue = if (uiState.isWarning) Color(0x33FF0000) else Color.Transparent, label = ""
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLuongAComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --- ẢNH NỀN ---
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- NỘI DUNG CHÍNH ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 32.dp, start = 16.dp, end = 16.dp), // Thu hẹp padding lề để thanh bão hòa tối đa màn hình
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- 1. HEADER HIỂN THỊ THỜI GIAN ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = timeString,
                    color = Color(0xFF3A1240),
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString,
                    color = Color(0xFF3A1240).copy(alpha = 0.85f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // --- 2. KHUNG CAMERA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.Black)
                    .onGloballyPositioned { coordinates ->
                        previewWidth = coordinates.size.width.toFloat()
                        previewHeight = coordinates.size.height.toFloat()
                    },
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), FaceMissionAnalyzer { isQualified, ear, smileProb, leftEye, rightEye, mouth, w, h ->
                                    viewModel.updateFaceTracking(isQualified, ear, smileProb, leftEye, rightEye, mouth, w, h)
                                })

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalysis)
                                } catch (e: Exception) { e.printStackTrace() }
                            }, ContextCompat.getMainExecutor(ctx))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Box(modifier = Modifier.fillMaxSize().background(warningOverlayColor))

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (previewWidth > 0 && previewHeight > 0 && uiState.cameraWidth > 0 && uiState.cameraHeight > 0) {
                        val scaleX = previewWidth / uiState.cameraWidth.toFloat()
                        val scaleY = previewHeight / uiState.cameraHeight.toFloat()

                        val neonCyan = Color(0xFF00FFFF)
                        val warningColor = Color(0xFFFF0055)

                        val eyeColor = if (uiState.isEyeOpenValid) neonCyan else warningColor
                        val mouthColor = if (uiState.isSmileValid) neonCyan else warningColor

                        uiState.leftEyePoint?.let { pt ->
                            val correctedX = previewWidth - (pt.x * scaleX)
                            val correctedY = pt.y * scaleY
                            drawCircle(color = eyeColor, radius = 24.dp.toPx(), center = Offset(correctedX, correctedY), style = Stroke(width = 3.dp.toPx()))
                        }

                        uiState.rightEyePoint?.let { pt ->
                            val correctedX = previewWidth - (pt.x * scaleX)
                            val correctedY = pt.y * scaleY
                            drawCircle(color = eyeColor, radius = 24.dp.toPx(), center = Offset(correctedX, correctedY), style = Stroke(width = 3.dp.toPx()))
                        }

                        uiState.mouthPoint?.let { pt ->
                            val correctedX = previewWidth - (pt.x * scaleX)
                            val correctedY = pt.y * scaleY
                            drawCircle(color = mouthColor, radius = 35.dp.toPx(), center = Offset(correctedX, correctedY), style = Stroke(width = 3.dp.toPx()))
                        }
                    }
                }
            }

            // --- 3. FOOTER: PROGRESS BAR ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isWarning) uiState.warningMessage else "Mở to mắt và mỉm cười để tắt báo thức",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp, top = 20.dp)
                )

                // Tăng chiều cao lên 80.dp giúp nền cọ mở rộng hiển thị to rõ hơn
                CustomPaintProgressBar(
                    progress = uiState.progress,
                    bgResId = R.drawable.brush_rainbow,
                    borderColor = Color(0xFF9C2B81),
                    borderWidth = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                )
            }
        }
    }
}