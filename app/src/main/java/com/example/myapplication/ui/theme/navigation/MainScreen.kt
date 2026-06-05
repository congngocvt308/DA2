package com.example.myapplication.ui.theme.navigation

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.alarm_logic.AlarmService
import com.example.myapplication.ui.theme.alarm.AlarmRingingScreen
import com.example.myapplication.ui.theme.alarm.AlarmScreen
import com.example.myapplication.ui.theme.alarm.AlarmSettingsScreen
import com.example.myapplication.ui.theme.alarm.QuizScreen
import com.example.myapplication.ui.theme.components.PermissionItem
import com.example.myapplication.ui.theme.stats.StatsScreen
import com.example.myapplication.ui.theme.topic.TopicDetailScreen
import com.example.myapplication.ui.theme.topic.TopicScreen

@Composable
fun MainScreen(initialExternalRoute: String? = null) {

    val navController = rememberNavController()
    LaunchedEffect(initialExternalRoute) {
        if (initialExternalRoute == Screen.QUIZ_SCREEN) {
            navController.navigate(Screen.QUIZ_SCREEN) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // --- STATE THEO DÕI QUYỀN ---
    var hasAlarmPermission by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Hàm cập nhật trạng thái các quyền
    fun updatePermissionStates() {
        hasAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        hasCameraPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        // Chỉ hiện Dialog nếu thiếu 1 trong các quyền quan trọng
        showPermissionDialog = !hasAlarmPermission || !hasCameraPermission || !hasNotificationPermission
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { updatePermissionStates() }



    // Kiểm tra ngay khi vào app
    LaunchedEffect(Unit) {
        val permissions = mutableListOf(android.Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
        updatePermissionStates()
    }

    // Kiểm tra lại khi quay lại app từ Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { updatePermissionStates() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- DIALOG CUSTOM VỚI PERMISSION ITEMS ---
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {showPermissionDialog = false},
            title = { Text("Yêu cầu hệ thống 🚨", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Vui lòng cấp các quyền dưới đây để app hoạt động chuẩn xác:")
                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionItem(
                        title = "Báo thức & Nhắc nhở",
                        description = "Đảm bảo reo đúng từng giây",
                        icon = Icons.Default.Alarm,
                        isGranted = hasAlarmPermission,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )

                    // 1. Quyền Máy ảnh
                    // 1. Quyền Máy ảnh
                    PermissionItem(
                        title = "Máy ảnh",
                        description = "Dùng để quét mã QR tắt báo thức",
                        icon = Icons.Default.CameraAlt,
                        isGranted = hasCameraPermission,
                        onClick = {
                            if (!hasCameraPermission) {
                                // Nếu hệ thống không cho hiện popup nữa, ta mở Cài đặt
                                // Bạn có thể dùng ActivityCompat.shouldShowRequestPermissionRationale để check kỹ hơn
                                permissionLauncher.launch(arrayOf(android.Manifest.permission.CAMERA))

                                openAppSettings(context)
                            }
                        }
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionItem(
                            title = "Thông báo",
                            description = "Hiển thị báo thức và lời nhắc",
                            icon = Icons.Default.Notifications,
                            isGranted = hasNotificationPermission,
                            onClick = {
                                if (!hasNotificationPermission) {
                                    permissionLauncher.launch(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS))
                                    openAppSettings(context)
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("Đóng") }
            }
        )
    }

    // --- PHẦN GIAO DIỆN CHÍNH (GIỮ NGUYÊN) ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(Screen.ALARM_TAB, Screen.TOPIC_TAB, Screen.STATS_TAB)

    Scaffold(
        bottomBar = { if (showBottomBar) BottomNavBar(navController = navController)},
        containerColor = MaterialTheme.colorScheme.background
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
                StatsScreen()
            }

            composable(Screen.QUIZ_SCREEN) {

                QuizScreen(
                    viewModel = viewModel(),
                    onBack = {
                        navController.popBackStack()
                    },
                    onQuizCompleted = {
                        val intent = Intent(context, AlarmService::class.java)
                        context.stopService(intent)
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

// Đặt ở cuối file MainScreen.kt, ngoài các class/function khác
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}