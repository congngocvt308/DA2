package com.cong.smartalarm.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Token màu "Đêm → Bình minh" (UI UX Pro Max – Alarm & World Clock: nền tối, điểm nhấn hổ phách,
 * tránh nền trắng thuần). App được mở chủ yếu lúc tối trước khi ngủ, nên nền xanh đêm dịu mắt;
 * hổ phách là màu nắng sớm cho hành động chính; chàm gợi đêm cho phần phụ.
 * Riêng màn chuông reo dùng bộ [Dawn] sáng ấm để đánh thức.
 *
 * Mọi cặp chữ / nền đạt ≥ 4.5:1. Trên nền hổ phách / xanh lá / đỏ, chữ phải là màu On* (tối).
 * Màn hình chỉ dùng token (hoặc MaterialTheme.colorScheme), không viết Color(0x…) trực tiếp.
 */
object Calm {
    /** Hổ phách: nút chính, công tắc bật, tab đang chọn, giờ cần nhấn mạnh. */
    val Primary = Color(0xFFF59E0B)
    /** Chữ / biểu tượng đặt trên nền [Primary]. */
    val OnPrimary = Color(0xFF0F172A)
    /** Chữ đặt trên nền sáng có sẵn (giấy ảnh polaroid, khung ảnh pastel). */
    val OnLight = Color(0xFF0F172A)

    /** Chữ chính. */
    val Foreground = Color(0xFFF1F5F9)
    /** Chữ phụ. */
    val MutedForeground = Color(0xFF94A3B8)

    /** Chàm: điểm nhấn phụ, cột biểu đồ, đầu dải gradient. */
    val Accent = Color(0xFF818CF8)
    /** Viền chọn, rãnh trống, chỉ báo phụ. Không dùng làm màu chữ. */
    val Subtle = Color(0xFF334155)
    /** Nền nổi hơn thẻ một bậc: chip, ô, hàng được chọn. */
    val Raised = Color(0xFF273449)

    /** Xanh lá: thành công, đạt mục tiêu (tô nền / biểu tượng). */
    val Wellness = Color(0xFF34D399)
    /** Chữ xanh lá trên nền tối. */
    val WellnessText = Color(0xFF34D399)
    val WellnessMist = Color(0xFF133A33)
    /** Chữ / biểu tượng đặt trên nền [Wellness]. */
    val OnWellness = Color(0xFF052E22)

    /** Nền màn hình. */
    val Background = Color(0xFF0F172A)
    /** Thẻ, dialog, thanh điều hướng. */
    val Surface = Color(0xFF1E293B)
    /** Rãnh công tắc tắt, thanh tiến độ trống. */
    val Muted = Color(0xFF1B2538)
    val Border = Color(0xFF2A3A52)

    /** Đỏ (chữ, biểu tượng, nền nút xoá). */
    val Destructive = Color(0xFFF87171)
    val DestructiveMist = Color(0xFF3A1D24)
    /** Chữ / biểu tượng đặt trên nền [Destructive]. */
    val OnDestructive = Color(0xFF450A0A)

    /** Vàng cảnh báo nhẹ (đủ sáng để làm chữ trên nền tối). */
    val Warm = Color(0xFFFBBF24)
    val WarmMist = Color(0xFF3A2E12)

    /** Bóng của bề mặt nổi trên nền tối. */
    val ShadowDark = Color(0xFF020617)
    /** Viền sáng rất mảnh phía trên bề mặt (ánh trăng). */
    val ShadowLight = Color(0xFFFFFFFF)
}

/** Bộ màu sáng ấm chỉ dành cho màn chuông reo (bầu trời bình minh). */
object Dawn {
    val SkyTop = Color(0xFFFFE4C7)
    val SkyMid = Color(0xFFFFD7A8)
    val SkyHorizon = Color(0xFFFFC07A)
    val Sun = Color(0xFFFFB347)
    /** Biểu tượng mặt trời trên bề mặt sáng (đậm hơn [Sun] để thấy rõ). */
    val SunIcon = Color(0xFFD97706)
    /** Chữ chính trên nền bình minh. */
    val Text = Color(0xFF3B2412)
    /** Chữ phụ trên nền bình minh. */
    val MutedText = Color(0xFF6B4A2E)
    /** Bề mặt kính sữa trên nền bình minh. */
    val Surface = Color(0xE6FFF7ED)
}

internal val CalmColorScheme = darkColorScheme(
    primary = Calm.Primary,
    onPrimary = Calm.OnPrimary,
    primaryContainer = Color(0xFF3B3220),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = Calm.Accent,
    onSecondary = Calm.OnPrimary,
    secondaryContainer = Color(0xFF2E3463),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = Calm.WellnessText,
    onTertiary = Calm.OnWellness,
    tertiaryContainer = Calm.WellnessMist,
    onTertiaryContainer = Color(0xFFA7F3D0),
    // Trong suốt để thấy bầu trời đêm của CalmBackground phía sau Scaffold
    background = Color.Transparent,
    onBackground = Calm.Foreground,
    surface = Calm.Surface,
    onSurface = Calm.Foreground,
    surfaceVariant = Calm.Raised,
    onSurfaceVariant = Calm.MutedForeground,
    surfaceTint = Color.Transparent,
    surfaceBright = Calm.Raised,
    surfaceDim = Calm.Background,
    surfaceContainerLowest = Calm.Background,
    surfaceContainerLow = Color(0xFF172033),
    surfaceContainer = Calm.Surface,
    surfaceContainerHigh = Calm.Surface,
    surfaceContainerHighest = Calm.Raised,
    inverseSurface = Calm.Foreground,
    inverseOnSurface = Calm.Background,
    inversePrimary = Color(0xFFB45309),
    // Viền ô nhập cần ≥ 3:1 trên nền thẻ
    outline = Color(0xFF7C8BA1),
    outlineVariant = Calm.Border,
    error = Calm.Destructive,
    onError = Calm.OnDestructive,
    errorContainer = Calm.DestructiveMist,
    onErrorContainer = Color(0xFFFECACA),
    scrim = Color(0xFF000000)
)
