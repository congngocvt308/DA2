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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.alarm.AlarmRingingScreen
import com.example.myapplication.ui.theme.alarm.AlarmScreen
import com.example.myapplication.ui.theme.alarm.AlarmSettingsScreen
import com.example.myapplication.ui.theme.alarm.QuizScreen
import com.example.myapplication.ui.theme.topic.TopicDetailScreen
import com.example.myapplication.ui.theme.topic.TopicScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.ALARM_TAB,
        Screen.TOPIC_TAB,
        Screen.STATS_TAB
    )

    Scaffold(
    bottomBar = { if (showBottomBar) BottomNavBar(navController = navController)},
    containerColor = Color.Black
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.ALARM_TAB,
            modifier = Modifier.padding(bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp)
        ) {
            composable(Screen.ALARM_TAB) {
                AlarmScreen(
                    onNavigateToSettings = { alarmId ->
                        navController.navigate(Screen.alarmSettingsRoute(alarmId))
                    },
                    navController = navController
                )
            }

            composable(Screen.TOPIC_TAB) {
                TopicScreen(
                    onNavigateToDetail = { topicId ->
                        navController.navigate(Screen.topicDetailRoute(topicId))
                    }
                )
            }

            composable(Screen.STATS_TAB) {
//                StatsScreen()
            }

            composable(Screen.QUIZ_SCREEN) {

                QuizScreen(
                    viewModel = viewModel(),
                    onBack = {
                        navController.popBackStack()
                    },
                    onQuizCompleted = {
                        navController.navigate(Screen.ALARM_TAB) {
                            popUpTo(Screen.ALARM_RINGING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ALARM_RINGING) {
                AlarmRingingScreen(
                    alarmLabel = "Thức dậy đi học",
                    onSnooze = { /* Logic Snooze */ },
                    onNavigateToQuiz = {
                        navController.navigate(Screen.QUIZ_SCREEN)
                    },
                    onFinish = {
                    }
                )
            }

            composable(
                route = Screen.ALARM_SETTINGS,
                arguments = listOf(navArgument("alarmId") { type = NavType.IntType; defaultValue = -1 })
            ) {
                AlarmSettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMissionSettingClick = {
                        navController.navigate(Screen.MISSION_SELECTION)
                    }
                )
            }

            composable(
                route = Screen.TOPIC_DETAIL,
                arguments = listOf(navArgument("topicId") { type = NavType.IntType })
            ) { backStackEntry ->
                TopicDetailScreen(
                    onBackClick = { navController.popBackStack() }
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
                    unselectedIconColor = MaterialTheme.colorScheme.tertiary,
                    unselectedTextColor =MaterialTheme.colorScheme.tertiary,
                    indicatorColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}