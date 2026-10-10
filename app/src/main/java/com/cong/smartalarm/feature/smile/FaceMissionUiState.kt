package com.cong.smartalarm.feature.smile

import android.graphics.PointF

data class FaceMissionUiState(
    val progress: Float = 0f,
    val isWarning: Boolean = false,
    val warningMessage: String = "",
    val isSuccess: Boolean = false, // Khóa thứ nhất (Luồng A) mở thành công

    // Tọa độ các điểm mốc (Giữ nguyên của bạn)
    val leftEyePoint: PointF? = null,
    val rightEyePoint: PointF? = null,
    val mouthPoint: PointF? = null,

    // Trạng thái mắt/miệng (Bổ sung thêm tách riêng 2 mắt nếu cần)
    val isEyeOpenValid: Boolean = false,   // Trạng thái mắt mở chung (hoặc mắt trái)
    val isLeftEyeOpen: Boolean = false,    // Mới: Mắt trái mở (True = Hiện icon mắt mở trái, False = Hiện icon mắt nhắm)
    val isRightEyeOpen: Boolean = false,   // Mới: Mắt phải mở (True = Hiện icon mắt mở phải, False = Hiện icon mắt nhắm)
    val isSmileValid: Boolean = false,     // True = Cười (smile1.png), False = Không cười (un smile.png)

    // Mới: Độ rộng khuôn mặt hoặc khoảng cách 2 mắt (dùng để scale kích thước filter to/nhỏ theo cự ly)
    val faceWidthPx: Float = 300f,
    val headRotationZ: Float = 0f,

    val cameraWidth: Int = 1,
    val cameraHeight: Int = 1,

    // --- BỔ SUNG CẤU HÌNH TỪ DATABASE ---
    val isConfigLoaded: Boolean = false,
    val backgroundImageUri: String? = null,
    val requiredHoldDurationSec: Int = 3,
    val eyeOpenSoundUri: String? = null,
    val smileSoundUri: String? = null,
    val comboSoundUri: String? = null,
    val holdPoseSoundUri: String? = null
)
