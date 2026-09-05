package com.cong.smartalarm.ui.theme.facedetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cong.smartalarm.R
import com.cong.smartalarm.data.FaceMissionUiState
import com.cong.smartalarm.data.analyzer.FaceMissionAnalyzer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import kotlinx.coroutines.delay
import java.io.File
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import java.io.FileOutputStream
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CustomPaintProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 3.dp // Bóp nhỏ borderWidth lại một chút cho thanh thoát giống Ảnh 2
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth() // Cho tràn full chiều rộng Box
                .height(40.dp)
        ) {
            val strokePx = borderWidth.toPx()

            val innerLeft = strokePx
            val innerTop = strokePx
            val innerWidth = size.width - (strokePx * 2)
            val innerHeight = size.height - (strokePx * 2)

            val innerRadius = innerHeight / 2

            // Dải màu gradient từ Hồng/Tím sang Cam -> Vàng -> Xanh lam (chuẩn màu Ảnh 2)
            val rainbowColors = listOf(
                Color(0xFFD81B60), // Hồng tím
                Color(0xFFFF5722), // Cam
                Color(0xFFFFC107), // Vàng
                Color(0xFF00BCD4)  // Xanh lam
            )

            val rainbowBrush = Brush.linearGradient(
                colors = rainbowColors,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f)
            )

            if (innerWidth > 0f && innerHeight > 0f) {
                // 1. NỀN BÊN TRONG: Vẽ chính dải màu Rainbow nhưng cho mờ (alpha = 0.35f)
                // Giúp phần chưa fill có màu loang mờ rất sang chứ không bị xám xịt/tối nữa!
                drawRoundRect(
                    brush = rainbowBrush,
                    topLeft = Offset(innerLeft, innerTop),
                    size = Size(innerWidth, innerHeight),
                    cornerRadius = CornerRadius(innerRadius, innerRadius),
                    alpha = 0.35f
                )

                // 2. TIẾN TRÌNH (FILL): Vẽ dải màu đậm (alpha = 1f)
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

            // 3. ĐƯỜNG VIỀN: Dùng luôn dải màu Rainbow để viền rực rỡ, không bị chìm!
            val borderLeft = strokePx / 2
            val borderTop = strokePx / 2
            val borderWidthSize = size.width - strokePx
            val borderHeightSize = size.height - strokePx
            val borderRadius = borderHeightSize / 2

            drawRoundRect(
                brush = rainbowBrush, // Đổi từ Color đơn sắc sang Brush Gradient
                topLeft = Offset(borderLeft, borderTop),
                size = Size(borderWidthSize, borderHeightSize),
                cornerRadius = CornerRadius(borderRadius, borderRadius),
                style = Stroke(width = strokePx),
                alpha = 0.6f // Viền sáng vừa phải, đẹp mắt
            )
        }
    }
}

@Composable
fun FaceFilterOverlay(
    uiState: FaceMissionUiState,
    previewWidth: Float,
    previewHeight: Float,
    modifier: Modifier = Modifier
) {
    // 1. Load các ảnh từ Drawable (đã đổi tên chuẩn)
    val imgLeftEyeOpen = ImageBitmap.imageResource(id = R.drawable.ic_eye_left_open)   // left eye.png
    val imgRightEyeOpen = ImageBitmap.imageResource(id = R.drawable.ic_eye_right_open) // right eye.png
    val imgEyeClosed = ImageBitmap.imageResource(id = R.drawable.ic_eye_closed)       // mắt nhắm
    val imgMouthSmile = ImageBitmap.imageResource(id = R.drawable.ic_mouth_smile)     // smile1.png
    val imgMouthUnsmile = ImageBitmap.imageResource(id = R.drawable.ic_mouth_unsmile) // un smile.png

    Canvas(modifier = modifier.fillMaxSize()) {
        if (previewWidth > 0 && previewHeight > 0 && uiState.cameraWidth > 0 && uiState.cameraHeight > 0) {
            val scaleX = previewWidth / uiState.cameraWidth.toFloat()
            val scaleY = previewHeight / uiState.cameraHeight.toFloat()

            val scaledFaceWidth = uiState.faceWidthPx * scaleX

            // Dynamic Size filter
            val baseScale = (scaledFaceWidth / (previewWidth * 0.45f)).coerceIn(0.5f, 3.0f)
            val openEyeSize = (55.dp.toPx() * baseScale).toInt()
            val closedEyeSize = (45.dp.toPx() * baseScale).toInt()
            val closedEyeOffsetY = 8.dp.toPx() * baseScale
            val mouthWidth = (70.dp.toPx() * baseScale).toInt()  // Chiều ngang miệng
            val mouthHeight = (35.dp.toPx() * baseScale).toInt()
            val rotationAngle = uiState.headRotationZ

            // ----------------------------------------------------
            // 2. VẼ FILTER MẮT TRÁI
            // ----------------------------------------------------
            uiState.leftEyePoint?.let { pt ->
                val correctedX = previewWidth - (pt.x * scaleX)
                var correctedY = pt.y * scaleY

                val bitmap = if (uiState.isLeftEyeOpen ) imgLeftEyeOpen else imgEyeClosed

                val currentEyeSize = if (uiState.isLeftEyeOpen) openEyeSize else closedEyeSize
                if (!uiState.isLeftEyeOpen) {
                    correctedY += closedEyeOffsetY // Hạ vị trí Y xuống một chút
                }

                withTransform({
                    rotate(degrees = rotationAngle, pivot = Offset(correctedX, correctedY))
                }) {
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(
                            x = (correctedX - currentEyeSize / 2).toInt(),
                            y = (correctedY - currentEyeSize / 2).toInt()
                        ),
                        dstSize = IntSize(currentEyeSize, currentEyeSize)
                    )
                }
            }

            // MẮT PHẢI: Dùng riêng trạng thái isRightEyeOpen
            uiState.rightEyePoint?.let { pt ->
                val correctedX = previewWidth - (pt.x * scaleX)
                var correctedY = pt.y * scaleY

                val bitmap = if (uiState.isRightEyeOpen) imgRightEyeOpen else imgEyeClosed

                val currentEyeSize = if (uiState.isRightEyeOpen) openEyeSize else closedEyeSize
                if (!uiState.isRightEyeOpen) {
                    correctedY += closedEyeOffsetY // Hạ vị trí Y xuống một chút
                }

                withTransform({
                    rotate(degrees = rotationAngle, pivot = Offset(correctedX, correctedY))
                }) {
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(
                            x = (correctedX - currentEyeSize / 2).toInt(),
                            y = (correctedY - currentEyeSize / 2).toInt()
                        ),
                        dstSize = IntSize(currentEyeSize, currentEyeSize)
                    )
                }
            }



            // ----------------------------------------------------
            // 4. VẼ FILTER MIỆNG
            // ----------------------------------------------------
            uiState.mouthPoint?.let { pt ->
                val correctedX = previewWidth - (pt.x * scaleX)
                var correctedY = pt.y * scaleY
                if (!uiState.isSmileValid) {
                    val offsetY = 8.dp.toPx() * baseScale // Thay đổi 12.dp (ví dụ: 15.dp hoặc 20.dp) để hạ thấp thêm
                    correctedY += offsetY
                }
                val bitmap = if (uiState.isSmileValid) imgMouthSmile else imgMouthUnsmile

                withTransform({
                    rotate(degrees = rotationAngle, pivot = Offset(correctedX, correctedY))
                }) {
                    drawImage(
                        image = bitmap,
                        dstOffset = IntOffset(
                            x = (correctedX - mouthWidth / 2).toInt(),
                            y = (correctedY - mouthHeight / 2).toInt()
                        ),
                        dstSize = IntSize(mouthWidth, mouthHeight)
                    )
                }
            }
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
        targetValue = if (uiState.isWarning) Color(0x33FF0000) else Color.Transparent, label = ""
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
                .padding(top = 50.dp, bottom = 10.dp, start = 32.dp, end = 32.dp), // Thu hẹp padding lề để thanh bão hòa tối đa màn hình
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

            Spacer(modifier = Modifier.height(80.dp))

            // --- 2. KHUNG CAMERA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
                    .background(
                        color = Color.White.copy(alpha = 0.6f), // Màu viền mờ nhẹ
                        shape = RoundedCornerShape(40.dp)
                    )
                    .padding(2.5.dp)
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

                // 🎯 1. ANIMATION NHẤP NHÁY MÀU & THU PHÓNG CHO ICON CAMERA
                val cameraPulseTransition = rememberInfiniteTransition(label = "CameraIconPulse")

// Đổ bóng/Mờ dần alpha từ 0.4f -> 1.0f
                val iconAlpha by cameraPulseTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "IconAlpha"
                )

// Ph phóng nhẹ kích thước từ 0.9x -> 1.15x
                val iconScale by cameraPulseTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "IconScale"
                )

// 🎯 2. ICON BUTTON CHỤP ẢNH CÓ HIỆU ỨNG NHẤP NHÁY
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
                        .scale(iconScale) // 🚀 Hiệu ứng nhịp đập phình to/thu nhỏ
                        .background(
                            color = Color.Black.copy(alpha = 0.35f * iconAlpha),// 🚀 Viền/Nền hồng nhấp nháy mờ tỏ
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Chụp ảnh",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // --- 3. FOOTER: PROGRESS BAR ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.warningMessage.ifEmpty { "Mở to mắt và mỉm cười để tắt báo thức" },
                    color = if (uiState.isWarning) Color(0xFFFFD54F) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp, top = 20.dp)
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

fun takePhotoToInternalStorage(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoSaved: (imageUri: String, description: String) -> Unit
) {
    // 1. Tạo thư mục ẩn riêng trong App
    val photoFolder = File(context.filesDir, "alarm_photos").apply {
        if (!exists()) mkdirs()
    }

    // 2. Tạo file .nomedia chặn Gallery ứng dụng khác quét
    val noMediaFile = File(photoFolder, ".nomedia")
    if (!noMediaFile.exists()) {
        try { noMediaFile.createNewFile() } catch (e: Exception) { e.printStackTrace() }
    }

    val now = Date()
    // 🎯 3. Định dạng tên file theo chuẩn "Giờ:Phút Ngày-Tháng" (Ví dụ: 15:30 25-07.jpg)
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now)
    val photoFile = File(photoFolder, "IMG_$timeStamp.jpg")

    // 🎯 2. Tên mặc định hiển thị trên Card/Dialog chỉ có "Giờ:Phút" (Ví dụ: 11:16)
    val displayDescription = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                // 1. Chuyển ImageProxy thành Bitmap
                val bitmap = imageProxyToBitmap(image)
                image.close()

                // 2. Lật ngang Bitmap (Flip Horizontal) cho đúng góc nhìn gương
                val matrix = android.graphics.Matrix().apply {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
                val flippedBitmap = android.graphics.Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )

                // 3. Ghi Bitmap đã lật vào File mang tên vừa đặt
                FileOutputStream(photoFile).use { out ->
                    flippedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }

                val savedUri = photoFile.absolutePath

                onPhotoSaved(savedUri, displayDescription)

                Toast.makeText(context, "Ảnh đã được lưu vào Bảo Tàng Nụ Cười ✨", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCapture", "Lỗi chụp ảnh: ${exception.message}", exception)
            }
        }
    )
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}