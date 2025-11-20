package com.example.myapplication.ui.theme.navigation

import SplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun AppNavigation() {
    var showSplashScreen by remember { mutableStateOf(true) }
    if (showSplashScreen) {
        SplashScreen(onTimeout = { showSplashScreen = false })
    } else {
        MainScreen()
    }

}