package com.example.myapplication.ui.theme.alarm

import com.example.myapplication.ui.theme.alarm.AlarmCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel = viewModel(),
    onNavigateToSettings: (Int) -> Unit,
    navController: NavHostController
) {
    val alarmList by viewModel.alarms.collectAsState()
    val currentSortType by viewModel.sortType.collectAsState()
    val headerText by viewModel.timeUntilNextAlarms.collectAsState()
    var isFabMenuOpen by remember { mutableStateOf(false) }
    var isMoreMenuOpen by remember { mutableStateOf(false) }
    var showQuickDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(enabled = isFabMenuOpen) { isFabMenuOpen = false }
            .padding(top = 50.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AlarmTopBar(
                currentSortType = currentSortType,
                isMenuOpen = isMoreMenuOpen,
                onMenuClick = { isMoreMenuOpen = true },
                onDismissMenu = { isMoreMenuOpen = false },
                onSortDefault = { viewModel.setSortType(SortType.DEFAULT)},
                onSortActive = { viewModel.setSortType(SortType.ACTIVE_FIRST)},
                onDeleteInactive = { viewModel.deleteInactiveAlarms()}
            )

            Text(
                text =headerText,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            )

            TestStartButton(navController = navController)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = alarmList, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarmData = alarm,
                        onToggle = { newState ->
                            viewModel.toggleAlarm(alarm.id, newState)
                        },
                        onCardClick = {
                            onNavigateToSettings(alarm.id)
                        },
                        onDelete = {
                            viewModel.deleteAlarm(alarm.id)
                        }
                    )
                }
            }
        }

        if (isFabMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFabMenuOpen = false }
            )
        }

        FabSpeedDial(
            isMenuOpen = isFabMenuOpen,
            onToggleMenu = { isFabMenuOpen = !isFabMenuOpen },
            onAddNewAlarm = {
                isFabMenuOpen = false
                onNavigateToSettings(-1)
            },
            onQuickAlarmClick = {
                isFabMenuOpen = false
                showQuickDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
        )

        if (showQuickDialog) {
            QuickAlarmDialog(
                onDismissRequest = { showQuickDialog = false },
                onSave = { minutes ->
                    viewModel.addQuickAlarm(minutes)
                    showQuickDialog = false
                }
            )
        }
    }
}

@Composable
private fun AlarmTopBar(
    currentSortType: SortType,
    isMenuOpen: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onSortDefault: () -> Unit,
    onSortActive: () -> Unit,
    onDeleteInactive: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Thiết lập báo thức",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Box {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tùy chọn",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = isMenuOpen,
                onDismissRequest = onDismissMenu,
                offset = DpOffset(x = 0.dp, y = 0.dp),
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                DropdownMenuItem(
                    text = { Text("Sắp xếp", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp) },
                    onClick = {},
                    trailingIcon = { Icon(Icons.Default.Sort, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(15.dp)) }
                )

                DropdownMenuItem(
                    text = { Text("Mặc định", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onSortDefault()
                        onDismissMenu()
                    },
                    trailingIcon = {
                        if(currentSortType == SortType.DEFAULT)
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.secondary)
                        else null
                    }
                )

                DropdownMenuItem(
                    text = { Text("Đang hoạt động trước", color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onSortActive()
                        onDismissMenu()
                    },
                    trailingIcon = {
                        if(currentSortType == SortType.ACTIVE_FIRST)
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.secondary)
                        else null
                    }
                )

                HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surface)

                DropdownMenuItem(
                    text = { Text("Xóa báo thức không hoạt động", color = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        onDeleteInactive()
                        onDismissMenu
                    },
                    trailingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.primary )}
                )
            }
        }
    }
}

@Composable
fun FabSpeedDial(
    isMenuOpen: Boolean,
    onToggleMenu: () -> Unit,
    onAddNewAlarm: () -> Unit,
    onQuickAlarmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isMenuOpen) 45f else 0f,
        label = "FabRotation"
    )

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = isMenuOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.onSurface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.width(250.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    FabMenuItem(
                        Icons.Default.FlashOn,
                        "Báo thức nhanh",
                        onClick ={
                            onToggleMenu()
                            onQuickAlarmClick()
                        }
                    )

                    FabMenuItem(
                        Icons.Default.Alarm,
                        "Báo thức",
                        onClick = {
                            onToggleMenu()
                            onAddNewAlarm()
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleMenu,
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Tạo mới",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
fun FabMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(25.dp))

        Spacer(modifier = Modifier.width(15.dp))

        Text(text, color = MaterialTheme.colorScheme.background, fontSize = 20.sp)
    }
}
@Composable
fun TestStartButton(navController: NavHostController) {
    Button(
        onClick = {
            navController.navigate(Screen.ALARM_RINGING)
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("TEST: REO CHUÔNG")
    }
}