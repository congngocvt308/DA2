package com.cong.smartalarm.feature.smile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.R

/** Lớp phủ Canvas vẽ filter mắt / miệng lên đúng vị trí landmark khuôn mặt trên khung camera. */
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
