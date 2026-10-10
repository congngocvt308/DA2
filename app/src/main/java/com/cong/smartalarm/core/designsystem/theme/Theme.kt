package com.cong.smartalarm.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.cong.smartalarm.R
import com.cong.smartalarm.core.designsystem.component.GlassHost

/**
 * 🌿 "Mental Health App" (UI UX Pro Max): Neumorphism + Accessible & Ethical.
 * Bản đơn sắc: đen làm chủ đạo (thay cho lavender), xanh lá wellness làm nhấn,
 * nền xám rất nhạt, chữ gần đen. Mọi cặp chữ/nền đạt ≥ 4.5:1.
 *
 * Tên component vẫn là Glass* để các màn hình cũ không phải đổi, nhưng giờ chúng
 * vẽ bề mặt nổi mềm (soft UI) thay cho kính.
 */
object Calm {
    // Tên Lavender* giữ lại cho mã cũ, giá trị nay là thang xám trung tính (zinc)
    /** Xám đậm — trang trí / viền focus. */
    val Lavender = Color(0xFF52525B)
    /** Đen chủ đạo: nền nút với chữ trắng. */
    val Primary = Color(0xFF18181B)
    val LavenderSoft = Color(0xFFD4D4D8)
    val LavenderMist = Color(0xFFF4F4F5)
    /** Xanh lá wellness: tô nền/biểu tượng. */
    val Wellness = Color(0xFF059669)
    /** Xanh lá đậm cho chữ trên nền sáng. */
    val WellnessText = Color(0xFF047857)
    val WellnessMist = Color(0xFFD1FAE5)
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val Foreground = Color(0xFF18181B)
    val Muted = Color(0xFFEDEFF9)
    val MutedForeground = Color(0xFF475569)
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

private val CalmColorScheme = lightColorScheme(
    primary = Calm.Primary,
    onPrimary = Color.White,
    primaryContainer = Calm.LavenderMist,
    onPrimaryContainer = Calm.Foreground,
    secondary = Color(0xFF3F3F46),
    onSecondary = Color.White,
    secondaryContainer = Calm.LavenderSoft,
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Calm.WellnessText,
    onTertiary = Color.White,
    tertiaryContainer = Calm.WellnessMist,
    onTertiaryContainer = Color(0xFF064E3B),
    // Trong suốt để thấy nền "thở" của GlassHost phía sau Scaffold
    background = Color.Transparent,
    onBackground = Calm.Foreground,
    surface = Calm.Surface,
    onSurface = Calm.Foreground,
    surfaceVariant = Color(0xFFF4F4F5),
    onSurfaceVariant = Calm.MutedForeground,
    surfaceTint = Color.Transparent,
    surfaceBright = Calm.Surface,
    surfaceDim = Color(0xFFEDEDEE),
    surfaceContainerLowest = Calm.Surface,
    surfaceContainerLow = Color(0xFFFCFCFC),
    surfaceContainer = Calm.Surface,
    surfaceContainerHigh = Calm.Surface,
    surfaceContainerHighest = Color(0xFFF4F4F5),
    inverseSurface = Color(0xFF18181B),
    inverseOnSurface = Calm.Background,
    inversePrimary = Calm.LavenderSoft,
    outline = Color(0xFF71717A),
    outlineVariant = Calm.Border,
    error = Calm.Destructive,
    onError = Color.White,
    errorContainer = Calm.DestructiveMist,
    onErrorContainer = Color(0xFF7F1D1D),
    scrim = Color(0xFF09090B)
)

@OptIn(ExperimentalTextApi::class)
private fun variableFont(res: Int, weight: Int) =
    Font(res, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

/** "Wellness Calm": Lora (tiêu đề, nét cong hữu cơ) + Raleway (nội dung). */
val Lora = FontFamily(
    variableFont(R.font.lora, 400),
    variableFont(R.font.lora, 500),
    variableFont(R.font.lora, 600),
    variableFont(R.font.lora, 700)
)

val Raleway = FontFamily(
    variableFont(R.font.raleway, 300),
    variableFont(R.font.raleway, 400),
    variableFont(R.font.raleway, 500),
    variableFont(R.font.raleway, 600),
    variableFont(R.font.raleway, 700),
    variableFont(R.font.raleway, 800)
)

// Raleway mặc định dùng số kiểu cổ (old-style) — ép số thẳng hàng, đều cột để giờ báo thức dễ đọc
private const val Numerals = "lnum, tnum"

private fun TextStyle.calm(family: FontFamily, minWeight: FontWeight = FontWeight.Normal) = copy(
    fontFamily = family,
    fontFeatureSettings = Numerals,
    fontWeight = maxOf(fontWeight ?: FontWeight.Normal, minWeight)
)

private val CalmTypography = Typography().let { t ->
    Typography(
        displayLarge = t.displayLarge.calm(Lora),
        displayMedium = t.displayMedium.calm(Lora),
        displaySmall = t.displaySmall.calm(Lora),
        headlineLarge = t.headlineLarge.calm(Lora, FontWeight.SemiBold),
        headlineMedium = t.headlineMedium.calm(Lora, FontWeight.SemiBold),
        headlineSmall = t.headlineSmall.calm(Lora, FontWeight.SemiBold),
        titleLarge = t.titleLarge.calm(Lora, FontWeight.SemiBold),
        // Raleway nét mảnh: nâng tối thiểu lên Medium/SemiBold để dễ đọc
        titleMedium = t.titleMedium.calm(Raleway, FontWeight.SemiBold),
        titleSmall = t.titleSmall.calm(Raleway, FontWeight.SemiBold),
        bodyLarge = t.bodyLarge.calm(Raleway, FontWeight.Medium),
        bodyMedium = t.bodyMedium.calm(Raleway, FontWeight.Medium),
        bodySmall = t.bodySmall.calm(Raleway, FontWeight.Medium),
        labelLarge = t.labelLarge.calm(Raleway, FontWeight.SemiBold),
        labelMedium = t.labelMedium.calm(Raleway, FontWeight.SemiBold),
        labelSmall = t.labelSmall.calm(Raleway, FontWeight.SemiBold)
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Giữ tham số cho tương thích; giao diện Calm luôn sáng
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = CalmColorScheme, typography = CalmTypography) {
        GlassHost(content = content)
    }
}
