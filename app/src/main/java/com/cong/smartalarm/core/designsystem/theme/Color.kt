package com.cong.smartalarm.core.designsystem.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Token màu của phong cách "Mental Health App" (UI UX Pro Max): Neumorphism + Accessible & Ethical.
 * Bản đơn sắc: đen làm chủ đạo, xanh lá wellness làm nhấn, nền xám rất nhạt, chữ gần đen.
 * Mọi cặp chữ / nền dùng trong app đạt tương phản ≥ 4.5:1.
 *
 * Màn hình chỉ dùng các token này (hoặc MaterialTheme.colorScheme), không viết Color(0x…) trực tiếp.
 */
object Calm {
    /** Đen chủ đạo: nền nút / phần được chọn, chữ trắng đặt lên. */
    val Primary = Color(0xFF18181B)
    /** Chữ chính. */
    val Foreground = Color(0xFF18181B)
    /** Chữ phụ (vẫn đạt 4.5:1 trên nền trắng). */
    val MutedForeground = Color(0xFF475569)

    /** Xám đậm: trang trí, cột biểu đồ, đầu dải gradient. */
    val Neutral = Color(0xFF52525B)
    /** Xám nhạt: viền chọn, rãnh trống, chỉ báo phụ. Không dùng làm màu chữ. */
    val NeutralSoft = Color(0xFFD4D4D8)
    /** Xám rất nhạt: nền chip / ô / mục được chọn. */
    val NeutralMist = Color(0xFFF4F4F5)

    /** Xanh lá wellness: tô nền / biểu tượng (thành công, đạt mục tiêu). */
    val Wellness = Color(0xFF059669)
    /** Xanh lá đậm cho chữ trên nền sáng. */
    val WellnessText = Color(0xFF047857)
    val WellnessMist = Color(0xFFD1FAE5)

    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val Muted = Color(0xFFEDEFF9)
    val Border = Color(0xFFE4E4E7)

    val Destructive = Color(0xFFDC2626)
    val DestructiveMist = Color(0xFFFEE2E2)
    /** Cam ấm (cảnh báo nhẹ), đủ đậm để làm chữ. */
    val Warm = Color(0xFFB45309)
    val WarmMist = Color(0xFFFEF3C7)

    /** Bóng tối của bề mặt nổi: xám trung tính, không dùng đen tuyền. */
    val ShadowDark = Color(0xFF71717A)
    val ShadowLight = Color(0xFFFFFFFF)
}

internal val CalmColorScheme = lightColorScheme(
    primary = Calm.Primary,
    onPrimary = Color.White,
    primaryContainer = Calm.NeutralMist,
    onPrimaryContainer = Calm.Foreground,
    secondary = Color(0xFF3F3F46),
    onSecondary = Color.White,
    secondaryContainer = Calm.NeutralSoft,
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Calm.WellnessText,
    onTertiary = Color.White,
    tertiaryContainer = Calm.WellnessMist,
    onTertiaryContainer = Color(0xFF064E3B),
    // Trong suốt để thấy nền "thở" của CalmBackground phía sau Scaffold
    background = Color.Transparent,
    onBackground = Calm.Foreground,
    surface = Calm.Surface,
    onSurface = Calm.Foreground,
    surfaceVariant = Calm.NeutralMist,
    onSurfaceVariant = Calm.MutedForeground,
    surfaceTint = Color.Transparent,
    surfaceBright = Calm.Surface,
    surfaceDim = Color(0xFFEDEDEE),
    surfaceContainerLowest = Calm.Surface,
    surfaceContainerLow = Color(0xFFFCFCFC),
    surfaceContainer = Calm.Surface,
    surfaceContainerHigh = Calm.Surface,
    surfaceContainerHighest = Calm.NeutralMist,
    inverseSurface = Calm.Primary,
    inverseOnSurface = Calm.Background,
    inversePrimary = Calm.NeutralSoft,
    outline = Color(0xFF71717A),
    outlineVariant = Calm.Border,
    error = Calm.Destructive,
    onError = Color.White,
    errorContainer = Calm.DestructiveMist,
    onErrorContainer = Color(0xFF7F1D1D),
    scrim = Color(0xFF09090B)
)
