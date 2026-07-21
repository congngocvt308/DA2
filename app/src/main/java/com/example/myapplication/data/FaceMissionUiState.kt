package com.example.myapplication.data

import android.graphics.PointF

data class FaceMissionUiState(
    val progress: Float = 0f,
    val isWarning: Boolean = false,
    val warningMessage: String = "",
    val isSuccess: Boolean = false, // Khóa thứ nhất (Luồng A) mở thành công
    val leftEyePoint: PointF? = null,
    val rightEyePoint: PointF? = null,
    val mouthPoint: PointF? = null,
    val isEyeOpenValid: Boolean = false,  // True = Xanh, False = Đỏ
    val isSmileValid: Boolean = false,    // True = Xanh, False = Đỏ
    val cameraWidth: Int = 1,
    val cameraHeight: Int = 1
)
