package com.example.myapplication.ui.theme.alarm

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.mission.MissionSelectionDialog
import com.example.myapplication.utils.RingtoneUtils
import com.example.myapplication.utils.SoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(
    viewModel: AlarmSettingsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onMissionSettingClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = Int.MAX_VALUE / 2)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = Int.MAX_VALUE / 2)
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showMissionDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    val previewPlayer = remember { SoundPlayer(context) }

    val ringtoneTitle = remember(uiState.ringtoneUri) {
        RingtoneUtils.getRingtoneTitle(context, uiState.ringtoneUri)
    }

    DisposableEffect(Unit) {
        onDispose { previewPlayer.stop() }
    }

    LaunchedEffect(Unit) {
        val startBase = Int.MAX_VALUE / 2
        val startHourIndex = startBase - (startBase % 24) + uiState.hour
        val startMinuteIndex = startBase - (startBase % 60) + uiState.minute
        hourListState.scrollToItem(startHourIndex)
        minuteListState.scrollToItem(startMinuteIndex)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
        }
    }

    BackHandler(enabled = true) {
        showDiscardDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text("Chuông báo thức", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {showDiscardDialog = true}) {
                        Icon(Icons.Default.Close, "Đóng", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ){
                item {
                    OutlinedTextField(
                        value = uiState.label,
                        onValueChange = { viewModel.onLabelChanged(it) },
                        label = { Text("Tên báo thức", color = MaterialTheme.colorScheme.tertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        leadingIcon = {
                            Image(
                                painter = painterResource(com.example.myapplication.R.drawable.sun),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Edit,
                                contentDescription = "Sửa tên",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                    )
                }

                // --- 1. PHẦN CHỈNH THỜI GIAN (2 LazyColumn) ---
                item {
                    TimePickerSection(
                        hourListState = hourListState,
                        minuteListState = minuteListState,
                        selectedHour = uiState.hour,
                        selectedMinute = uiState.minute,
                        onHourChange = { newHour -> viewModel.updateHour(newHour) },
                        onMinuteChange = { newMinute -> viewModel.updateMinute(newMinute) }
                    )
                }

                //--- 2. ĐỔ CHUÔNG SAU ---
                item {
                    Text(
                        text = uiState.timeUntilAlarm,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }

                //--- 3. CHỌN NGÀY ---
                item {
                    DaySelectorSection(
                        daysOfWeek = uiState.daysOfWeek,
                        onRepeatDailyChange = { isChecked ->
                            viewModel.toggleRepeatDaily(isChecked)
                        },
                        onDayToggle = { day ->
                            viewModel.toggleDay(day)
                        }
                    )
                }

                // --- 4. NHIỆM VỤ BÁO THỨC ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ){
                            SettingsSectionHeader(title = "Nhiệm vụ báo thức")
                            AlarmTaskSection(
                                questionCount = uiState.questionCount,
                                onAddClick = { showMissionDialog = true }
                            )
                        }
                    }
                }

                // --- 5. ÂM THANH BÁO THỨC ---
                item {
                    AlarmSoundSection(
                        currentSoundTitle = RingtoneUtils.getRingtoneTitle(context, uiState.ringtoneUri),
                        currentVolume = uiState.volume,

                        // Kéo thanh slider -> Cập nhật volume vào Data
                        onVolumeChange = { newVolume ->
                            viewModel.updateVolume(newVolume)
                            previewPlayer.playOrUpdateVolume(uiState.ringtoneUri, newVolume)
                        },

                        // Click vào tên bài hát -> Mở Dialog
                        onSoundClick = { showSoundDialog = true }
                    )
                }

                // --- 7. CÀI ĐẶT TÙY CHỈNH ---
                item {
                    SnoozeSettingsSection(
                        isSnoozeEnabled = uiState.isSnoozeEnabled,
                        snoozeDuration = uiState.snoozeDuration,
                        onSnoozeToggle = { viewModel.onSnoozeToggle(it) },
                        onDurationClick = { showSnoozeDialog = true }
                    )
                }
            }
        }
        Button(
            onClick = { viewModel.saveAlarm() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp)
                .height(56.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Lưu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        if (showDiscardDialog) {
            DiscardChangesDialog(
                onDismissRequest = { showDiscardDialog = false },
                onConfirmDiscard = {
                    showDiscardDialog = false
                    onBackClick()
                }
            )
        }
    }
    if (showSnoozeDialog) {
        SnoozeDurationDialog(
            currentDuration = uiState.snoozeDuration,
            onDismiss = { showSnoozeDialog = false },
            onDurationSelected = { newDuration ->
                viewModel.onSnoozeDurationChanged(newDuration)
                showSnoozeDialog = false
            }
        )
    }

    if (showMissionDialog) {
        MissionSelectionDialog(
            initialCount = uiState.questionCount,
            initialSelection = uiState.selectedQuestions,
            onDismiss = { showMissionDialog = false },
            onConfirm = { count, questions ->
                viewModel.updateMission(count, questions)
                showMissionDialog = false
            }
        )
    }

    if (showSoundDialog) {
        SoundSelectionDialog(
            currentUri = uiState.ringtoneUri,
            currentVolume = uiState.volume,
            onDismiss = { showSoundDialog = false },
            onConfirm = { newUri ->
                viewModel.updateRingtone(newUri)
            }
        )
    }
}

@Composable
fun VolumeSliderRow(volume: Float, onVolumeChange: (Float) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Âm lượng",
            tint = MaterialTheme.colorScheme.onSurface
        )
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onSurface,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = MaterialTheme.colorScheme.background,
            )
        )
    }
}

@Composable
fun SoundSelectionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Mở danh sách nhạc */ }
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "TOKUSOU SENTAI DEKAR...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AlarmTaskSection(
    questionCount: Int,
    onAddClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(modifier = Modifier){
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp, start = 20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ){
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                        }
                        Text(
                            text = if (questionCount > 0) "$questionCount câu" else "Thêm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
        items(3) {
            Box(modifier = Modifier){
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier
                        .size(80.dp)
                        .padding(top = 4.dp, end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp, start = 20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ){
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.background)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimePickerSection(
    hourListState: LazyListState,
    minuteListState: LazyListState,
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
){
    RememberInfiniteSnap(hourListState, 24, onHourChange)
    RememberInfiniteSnap(minuteListState, 60, onMinuteChange)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfiniteWheelColumn(
            state = hourListState,
            itemCount = 24,
            selectedItem = selectedHour,
            alignment = Alignment.End
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp).padding(top = 20.dp)
        )

        InfiniteWheelColumn(
            state = minuteListState,
            itemCount = 60,
            selectedItem = selectedMinute,
            alignment = Alignment.Start
        )
    }
}

@Composable
private fun InfiniteWheelColumn(
    state: LazyListState,
    itemCount: Int,
    selectedItem: Int,
    alignment: Alignment.Horizontal
) {
    LazyColumn(
        state = state,
        modifier = Modifier.wrapContentWidth(),
        horizontalAlignment = alignment,
        contentPadding = PaddingValues(vertical = 60.dp)
    ) {
        items(count = Int.MAX_VALUE) { index ->
            val value = index % itemCount
            val isSelected = (value == selectedItem)

            TimePickerItem(
                text = "%02d".format(value),
                isSelected = isSelected
            )
        }
    }
}

@Composable
private fun RememberInfiniteSnap(
    lazyListState: LazyListState,
    itemCount: Int,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            coroutineScope.launch {
                delay(50)
                val layoutInfo = lazyListState.layoutInfo
                if (layoutInfo.visibleItemsInfo.isEmpty()) return@launch
                val containerCenter = layoutInfo.viewportEndOffset / 2
                val closestItem = layoutInfo.visibleItemsInfo.minByOrNull {
                    abs((it.offset + it.size / 2) - containerCenter)
                }
                if (closestItem != null) {
                    lazyListState.animateScrollToItem(closestItem.index)
                    val actualValue = closestItem.index % itemCount
                    onItemSelected(actualValue)
                }
            }
        }
    }
}

@Composable
fun DaySelectorSection(
    daysOfWeek: Set<String>,
    onRepeatDailyChange: (Boolean) -> Unit,
    onDayToggle: (String) -> Unit
) {
    val isDaily = daysOfWeek.size == 7
    val selectedColor = MaterialTheme.colorScheme.secondary
    val onSelectedColor = MaterialTheme.colorScheme.onPrimary
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant
    val onUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dayOrder = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRepeatDailyChange(!isDaily) }
                .padding(top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hàng ngày",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Checkbox(
                checked = isDaily,
                onCheckedChange = { onRepeatDailyChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = selectedColor,
                    uncheckedColor = MaterialTheme.colorScheme.tertiary,
                    checkmarkColor = onSelectedColor
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dayOrder.forEach { day ->
                val isSelected = daysOfWeek.contains(day)
                CustomDayChip(
                    text = day,
                    isSelected = isSelected,
                    selectedColor = selectedColor,
                    onSelectedColor = onSelectedColor,
                    unselectedColor = unselectedColor,
                    onUnselectedColor = onUnselectedColor,
                    onClick = { onDayToggle(day) }
                )
            }
        }
    }
}

@Composable
fun CustomDayChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onSelectedColor: Color,
    unselectedColor: Color,
    onUnselectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) selectedColor else unselectedColor)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) onSelectedColor else onUnselectedColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 18.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TimePickerItem(
    text: String,
    isSelected: Boolean
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        fontSize = if (isSelected) 36.sp else 32.sp,
        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiary,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SnoozeSettingsSection(
    isSnoozeEnabled: Boolean,
    snoozeDuration: Int,
    onSnoozeToggle: (Boolean) -> Unit,
    onDurationClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Báo lại",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Switch(
                    checked = isSnoozeEnabled,
                    onCheckedChange = onSnoozeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                        checkedTrackColor = MaterialTheme.colorScheme.secondary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.tertiary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            AnimatedVisibility(
                visible = isSnoozeEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.Gray, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onDurationClick),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Khoảng thời gian",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$snoozeDuration phút",
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SnoozeDurationDialog(
    currentDuration: Int,
    onDismiss: () -> Unit,
    onDurationSelected: (Int) -> Unit
) {
    val options = listOf(5, 10, 15, 20, 25, 30)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Thời gian báo lại",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn {
                    items(options) { duration ->
                        val isSelected = (duration == currentDuration)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDurationSelected(duration) }
                                .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$duration phút",
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}