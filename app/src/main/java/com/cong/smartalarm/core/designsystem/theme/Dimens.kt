package com.cong.smartalarm.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Khoảng cách theo lưới 4dp. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

/** Bo góc chuẩn: chip 12, thẻ 16, nhóm cài đặt 28, dialog 28. */
object CalmShapes {
    val Chip = RoundedCornerShape(12.dp)
    val Card = RoundedCornerShape(16.dp)
    val Section = RoundedCornerShape(28.dp)
    val Dialog = RoundedCornerShape(28.dp)
}

/** Vùng chạm tối thiểu theo hướng dẫn trợ năng. */
val MinTouchTarget = 48.dp
