package com.cong.smartalarm.navigation

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cong.smartalarm.core.alarm.AlarmService
import com.cong.smartalarm.feature.alarmedit.AlarmSettingsScreen
import com.cong.smartalarm.feature.alarmlist.AlarmScreen
import com.cong.smartalarm.feature.museum.MemoryMuseumScreen
import com.cong.smartalarm.feature.quiz.QuizScreen
import com.cong.smartalarm.feature.quiz.QuizViewModel
import com.cong.smartalarm.feature.ringing.AlarmRingingScreen
import com.cong.smartalarm.feature.smile.FaceMissionScreen
import com.cong.smartalarm.feature.smile.FaceMissionViewModel
import com.cong.smartalarm.feature.stats.StatsScreen
import com.cong.smartalarm.feature.topic.TopicDetailScreen
import com.cong.smartalarm.feature.topic.TopicScreen

/** Khung chính: theo dõi quyền hệ thống, NavHost các màn hình và thanh điều hướng dưới. */
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
        PermissionDialog(
            hasAlarmPermission = hasAlarmPermission,
            hasCameraPermission = hasCameraPermission,
            hasNotificationPermission = hasNotificationPermission,
            onDismiss = { showPermissionDialog = false },
            onRequestPermissions = { permissionLauncher.launch(it) }
        )
    }


    // --- PHẦN GIAO DIỆN CHÍNH (GIỮ NGUYÊN) ---
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(Screen.ALARM_TAB, Screen.TOPIC_TAB, Screen.STATS_TAB)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = Screen.ALARM_TAB,
                modifier = Modifier.fillMaxSize()
            ) {
                // ... (giữ nguyên các composable bên trong NavHost)
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
                    StatsScreen(
                        onNavigateToMuseum = {
                            navController.navigate(Screen.MEMORY_MUSEUM)
                        }
                    )
                }

                composable(Screen.MEMORY_MUSEUM) {
                    MemoryMuseumScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.QUIZ_SCREEN) {

                    QuizScreen(
                        viewModel = viewModel(factory = QuizViewModel.Factory),
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
                        onDismiss = {
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
                    route = Screen.FACE_MISSION,
                    arguments = listOf(navArgument("alarmId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
                    val faceViewModel: FaceMissionViewModel = viewModel(
                        factory = FaceMissionViewModel.factory(alarmId)
                    )

                    FaceMissionScreen(
                        viewModel = faceViewModel,
                        onLuongAComplete = {
                            val intent = Intent(context, AlarmService::class.java)
                            context.stopService(intent)
                            navController.navigate(Screen.ALARM_TAB) {
                                popUpTo(Screen.ALARM_RINGING) { inclusive = true }
                            }
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

            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    }
}
