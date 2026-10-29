package com.cong.smartalarm.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.cong.smartalarm.R

@OptIn(ExperimentalTextApi::class)
private fun variableFont(res: Int, weight: Int) =
    Font(res, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

/** "Wellness Calm": Lora (tiêu đề, nét cong hữu cơ) + Raleway (nội dung). Cả hai hỗ trợ tiếng Việt. */
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

/** Chữ tròn, dày cho các màn "đánh thức" (chuông reo, hoàn thành) theo thiết kế. */
val Nunito = FontFamily(
    variableFont(R.font.nunito, 600),
    variableFont(R.font.nunito, 700),
    variableFont(R.font.nunito, 800),
    variableFont(R.font.nunito, 900)
)

// Raleway mặc định dùng số kiểu cổ (old-style) -> ép số thẳng hàng (lnum) để giờ báo thức dễ đọc.
// Lora vốn đã có số thẳng hàng; KHÔNG bật tnum cho Lora: tnum của Lora đổi cả dấu cách sang bản rộng
// (325 so với 263 đơn vị) làm tiêu đề bị giãn chữ.
private fun numeralsFor(family: FontFamily): String? = if (family == Raleway) "lnum" else null

private fun TextStyle.calm(family: FontFamily, minWeight: FontWeight = FontWeight.Normal) = copy(
    fontFamily = family,
    fontFeatureSettings = numeralsFor(family),
    fontWeight = maxOf(fontWeight ?: FontWeight.Normal, minWeight)
)

internal val CalmTypography = Typography().let { t ->
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
