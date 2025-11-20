package com.example.myapplication.ui.theme.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.alarm.AlarmScreen
import com.example.myapplication.ui.theme.topic.TopicScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
    bottomBar = { BottomNavBar(navController) },
    containerColor = Color.Black
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.ALARM_TAB,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.ALARM_TAB) {
                AlarmScreen(
                    onNavigateToSettings = { alarmId ->
                        navController.navigate(Screen.alarmSettingsRoute(alarmId))
                    }
                )
            }

            composable(Screen.TOPIC_TAB) {
                TopicScreen()
            }

            composable(Screen.STATS_TAB) {
//                StatsScreen()
            }

            composable(
                route = "alarm_settings/{alarmId}",
                arguments = listOf(navArgument("alarmId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("alarmId") ?: -1
//                AlarmSettingScreen(
//                    alarmId = id,
//                    onBack = { navController.popBackStack() }
//                )
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
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(90.dp)
    ) {
        items.forEachIndexed { index, item ->
            val route = routes[index]
            val isSelected = currentRoute == route

            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item, modifier = Modifier.size(24.dp)) },
                label = {
                    Text(
                        text = item,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
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
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedTextColor =MaterialTheme.colorScheme.surfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}