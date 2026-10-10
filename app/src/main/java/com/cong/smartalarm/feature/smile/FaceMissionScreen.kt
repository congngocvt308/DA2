package com.cong.smartalarm.feature.smile

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cong.smartalarm.R
import com.cong.smartalarm.core.designsystem.theme.Calm
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

/** Màn hình nhiệm vụ khuôn mặt: mở to mắt và mỉm cười trước camera để tắt báo thức. */
@OptIn(ExperimentalGetImage::class)
@Composable
fun FaceMissionScreen(viewModel: FaceMissionViewModel, onLuongAComplete: () -> Unit) {
    FaceMissionContent(viewModel, onLuongAComplete)
}

@Composable
private fun FaceMissionContent(viewModel: FaceMissionViewModel, onLuongAComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var previewWidth by remember { mutableStateOf(0f) }
    var previewHeight by remember { mutableStateOf(0f) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

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
        targetValue = if (uiState.isWarning) Calm.Destructive.copy(alpha = 0.2f) else Color.Transparent, label = ""
    )

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLuongAComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // --- ẢNH NỀN MƯỢT MÀ ---
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uiState.backgroundImageUri ?: R.drawable.background)
                .crossfade(400)
                .placeholder(R.drawable.background)
                .build(),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- NỘI DUNG CHÍNH ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 10.dp, start = 32.dp, end = 32.dp),
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
                    color = Calm.Foreground,
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString,
                    color = Calm.Foreground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // --- 2. KHUNG CAMERA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
                    .background(
                        color = Calm.Subtle, // Viền xám nhạt
                        shape = RoundedCornerShape(40.dp)
                    )
                    .padding(2.5.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0xFF09090B))
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
                                val captureUseCase = ImageCapture.Builder().build()
                                imageCapture = captureUseCase

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(
                                    Executors.newSingleThreadExecutor(),
                                    FaceMissionAnalyzer { isQualified, leftEyeProb, rightEyeProb, smileProb, leftEye, rightEye, mouth, faceWidth, rotationZ, w, h ->
                                        viewModel.updateFaceTracking(
                                            isQualified = isQualified,
                                            leftEyeProb = leftEyeProb,
                                            rightEyeProb = rightEyeProb,
                                            smileProb = smileProb,
                                            leftEye = leftEye,
                                            rightEye = rightEye,
                                            mouth = mouth,
                                            faceWidthPx = faceWidth,
                                            headRotationZ = rotationZ, // 👈 Bổ sung truyền góc xoay vào ViewModel
                                            cWidth = w,
                                            cHeight = h
                                        )
                                    }
                                )

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_FRONT_CAMERA,
                                        preview,
                                        imageAnalysis,
                                        captureUseCase
                                    )
                                } catch (e: Exception) { e.printStackTrace() }
                            }, ContextCompat.getMainExecutor(ctx))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Box(modifier = Modifier.fillMaxSize().background(warningOverlayColor))

                FaceFilterOverlay(
                    uiState = uiState,
                    previewWidth = previewWidth,
                    previewHeight = previewHeight
                )

                // Nút chụp ảnh: nền trắng mềm, không nhấp nháy
                IconButton(
                    onClick = {
                        imageCapture?.let { capture ->
                            takePhotoToInternalStorage(context, capture) { imageUri, description ->
                                viewModel.savePictureToDb(imageUri, description)
                            }
                        } ?: Toast.makeText(context, "Camera chưa sẵn sàng!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .size(48.dp)
                        .background(color = Calm.Surface, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Chụp ảnh",
                        tint = Calm.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // --- 3. FOOTER: PROGRESS BAR ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hướng dẫn đặt trên nền trắng mềm để luôn đọc được trên mọi ảnh nền
                Text(
                    text = uiState.warningMessage.ifEmpty { "Mở to mắt và mỉm cười để tắt báo thức" },
                    color = if (uiState.isWarning) Calm.Warm else Calm.Foreground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 16.dp, top = 20.dp)
                        .background(
                            color = if (uiState.isWarning) Calm.WarmMist else Calm.Surface.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )

                // Tăng chiều cao lên 80.dp giúp nền cọ mở rộng hiển thị to rõ hơn
                CustomPaintProgressBar(
                    progress = uiState.progress,
                    borderWidth = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                )
            }
        }
    }
}
