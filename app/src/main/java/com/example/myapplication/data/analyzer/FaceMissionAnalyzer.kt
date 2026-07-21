package com.example.myapplication.data.analyzer

import android.graphics.PointF
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.myapplication.data.FaceMissionConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceMissionAnalyzer(
    private val onFaceTracked: (
        isQualified: Boolean,
        ear: Float,
        smileProb: Float,
        leftEyePos: PointF?,
        rightEyePos: PointF?,
        mouthPos: PointF?,
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

                    // Lấy vị trí Landmark (Tọa độ Pixel thô trên cảm biến ảnh)
                    val leftEyePoint = face.getLandmark(FaceLandmark.LEFT_EYE)?.position
                    val rightEyePoint = face.getLandmark(FaceLandmark.RIGHT_EYE)?.position
                    val mouthPoint = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position

                    val isQualified = avgEar >= FaceMissionConfig.THRESHOLD_EAR &&
                            smileProb >= FaceMissionConfig.THRESHOLD_SMILE

                    onFaceTracked(
                        isQualified, avgEar, smileProb,
                        leftEyePoint, rightEyePoint, mouthPoint,
                        finalWidth, finalHeight
                    )
                } else {
                    onFaceTracked(false, 0f, 0f, null, null, null, finalWidth, finalHeight)
                }
            }
            .addOnFailureListener {
                onFaceTracked(false, 0f, 0f, null, null, null, finalWidth, finalHeight)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}