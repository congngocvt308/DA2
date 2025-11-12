package com.example.myapplication

import SplashScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.myapplication.Data.AlarmData
import com.example.myapplication.ui.theme.MyApplicationTheme

object Screen {
    const val ALARM_TAB = "alarm"
    const val TOPIC_TAB = "topic"
    const val STATS_TAB = "stats"
    const val ALARM_SETTINGS = "alarm_settings/{alarmId}"
    fun alarmSettingsRoute(alarmId: Int) = "alarm_settings/$alarmId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var showSplashScreen by remember { mutableStateOf(true) }
    if (showSplashScreen) {
        SplashScreen(onTimeout = { showSplashScreen = false })
    } else {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val alarmData = remember {
        mutableStateListOf(
            AlarmData(1, "Hàng ngày", "06:30", "Thức dậy", true),
            AlarmData(2, "Cuối tuần", "08:00", "Đi chơi", false),
            AlarmData(3, "Thứ 2, 4, 6", "14:00", "Học Flutter", true),
            AlarmData(4, "Chủ nhật", "22:00", null, true)
        )
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = Color.Black
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.ALARM_TAB,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.ALARM_TAB) {
                AlarmScreen(
                    alarmData = alarmData,
                    onToggle = { index, newState ->
                        alarmData[index] = alarmData[index].copy(isEnabled = newState)
                    },
                    onAlarmCardClick = { alarm ->
                        navController.navigate(Screen.alarmSettingsRoute(alarm.id))
                    }
                )
            }
            composable(Screen.TOPIC_TAB) {
                Topic(
                    // onTopicClicked = { ... },
                    // onMenuClicked = { ... },
                    // onAddNewTopic = { ... }
                )
            }
            composable(Screen.STATS_TAB) {
                Text("Màn hình Thống kê", color = Color.White)
            }
            composable(
                route = Screen.ALARM_SETTINGS,
                arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
            ) { backStackEntry ->
                val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
                AlarmSettingsScreen(
                    alarmId = alarmId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf("Báo thức", "Chủ đề", "Thống kê")
    val icons = listOf(Icons.Filled.Alarm, Icons.Filled.Style, Icons.Filled.Assessment)
    val routes = listOf(Screen.ALARM_TAB, Screen.TOPIC_TAB, Screen.STATS_TAB)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF1C1C1E),
        modifier = Modifier.height(90.dp)
    ) {
        items.forEachIndexed { index, item ->
            val route = routes[index]
            val isSelected = currentRoute == route

            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item, modifier = Modifier.size(24.dp)) },
                label = { Text(text = item, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFF8E8E93),
                    unselectedTextColor = Color(0xFF8E8E93),
                    indicatorColor = Color(0xFF1C1C1E)
                )
            )
        }
    }
}

@Preview(showBackground = true,
    showSystemUi = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}