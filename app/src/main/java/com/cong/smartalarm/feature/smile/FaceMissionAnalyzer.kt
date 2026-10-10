package com.cong.smartalarm.feature.smile

import android.graphics.PointF
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.cong.smartalarm.feature.smile.FaceMissionConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceMissionAnalyzer(
    private val onFaceTracked: (
        isQualified: Boolean,
        leftEyeProb: Float,   // Mới: Nhận xác suất mắt trái
        rightEyeProb: Float,  // Mới: Nhận xác suất mắt phải
        smileProb: Float,
        leftEyePos: PointF?,
        rightEyePos: PointF?,
        mouthPos: PointF?,
        faceWidthPx: Float,
        headRotationZ: Float,
        imageWidth: Int,
        imageHeight: Int
    ) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(options)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)

        // Vì Camera trước bị xoay dọc góc 90 hoặc 270 độ, kích thước khung ảnh thực tế đẩy sang UI
        // cần phải hoán đổi chiều ngang/dọc dựa trên góc xoay phần cứng.
        val isRotated = rotation == 90 || rotation == 270
        val finalWidth = if (isRotated) imageProxy.height else imageProxy.width
        val finalHeight = if (isRotated) imageProxy.width else imageProxy.height

        detector.process(image)
            .addOnSuccessListener { faces ->
                val face = faces.firstOrNull()
                if (face != null) {
                    val leftEye = face.leftEyeOpenProbability ?: 0f
                    val rightEye = face.rightEyeOpenProbability ?: 0f
                    val avgEar = (leftEye + rightEye) / 2f
                    val smileProb = face.smilingProbability ?: 0f
                    val faceWidth = face.boundingBox.width().toFloat()
                    val rotationZ = face.headEulerAngleZ

                    // Lấy vị trí Landmark (Tọa độ Pixel thô trên cảm biến ảnh)
                    val leftEyePoint = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
                    val rightEyePoint = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
                    val mouthPoint = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position

                    val isQualified = avgEar >= FaceMissionConfig.THRESHOLD_EAR &&
                            smileProb >= FaceMissionConfig.THRESHOLD_SMILE

                    onFaceTracked(
                        isQualified, leftEye, rightEye, smileProb,
                        leftEyePoint, rightEyePoint, mouthPoint,
                        faceWidth, rotationZ,
                        finalWidth, finalHeight
                    )
                } else {
                    onFaceTracked(false, 0f, 0f, 0f, null, null, null, 0f, 0f, finalWidth, finalHeight)
                }
            }
            .addOnFailureListener {
                onFaceTracked(false, 0f, 0f, 0f, null, null, null, 0f, 0f, finalWidth, finalHeight)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}