package com.cong.smartalarm.feature.smile

object FaceMissionConfig {
    // Ngưỡng mở mắt (0.0: nhắm hoàn toàn -> 1.0: mở to)
    const val THRESHOLD_EAR = 0.15f

    // Ngưỡng nụ cười (0.0: mặt lạnh -> 1.0: cười hết cỡ)
    const val THRESHOLD_SMILE = 0.3f

    // Tổng điểm cần đạt để xong nhiệm vụ (Số frame đúng liên tục)
    const val TARGET_SCORE = 60f

    // Tốc độ tăng / giảm điểm
    const val SCORE_INCREMENT = 1.0f  // Điểm cộng mỗi frame làm ĐÚNG
    const val SCORE_DECREMENT = 2.0f  // Điểm trừ mỗi frame làm SAI
}