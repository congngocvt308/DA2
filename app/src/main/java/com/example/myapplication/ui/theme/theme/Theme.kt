package com.example.myapplication.ui.theme.theme

import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val AppDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE50043),
    onPrimary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurface = Color.White,
    secondary = Color(0xFF42A5F5),
    tertiary = Color.LightGray
)

private val AppLightColorScheme = lightColorScheme(
    primary = Color(0xFFE50043),
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE5E5E5),
    onSurface = Color.Black,
    secondary = Color(0xFF42A5F5),
    tertiary = Color.LightGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Tắt mặc định để tránh crash trên Android cũ
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}