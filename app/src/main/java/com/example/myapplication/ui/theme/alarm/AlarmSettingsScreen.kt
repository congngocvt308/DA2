package com.example.myapplication.ui.theme.alarm

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.example.myapplication.ui.theme.components.PremiumPurchaseDialog
import com.example.myapplication.ui.theme.mission.MissionSelectionDialog
import com.example.myapplication.ui.theme.qrcode.QRCodeManagementDialog
import com.example.myapplication.ui.theme.qrcode.QRCodeScannerScreen
import com.example.myapplication.ui.theme.qrcode.QRCodeSelectionDialog
import com.example.myapplication.ui.theme.qrcode.QRCodeViewModel
import com.example.myapplication.utils.PremiumManager
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
    
    // Premium Manager
    val premiumManager = remember { PremiumManager.getInstance(context) }
    val isPremium by premiumManager.isPremium.collectAsState()
    
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
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showResetPremiumDialog by remember { mutableStateOf(false) }
    var showQRCodeManagement by remember { mutableStateOf(false) }
    var showQRCodeSelection by remember { mutableStateOf(false) }
    var showQRScanner by remember { mutableStateOf(false) }
    
    // State để lưu code từ camera scanner
    var scannedCodeFromCamera by remember { mutableStateOf<Pair<String, String>?>(null) }
    
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
                            SettingsSectionHeader(
                                title = "Nhiệm vụ báo thức",
                                isPremium = isPremium,
                                onLongPress = { showResetPremiumDialog = true }
                            )
                            AlarmTaskSection(
                                questionCount = uiState.questionCount,
                                isPremium = isPremium,
                                onAddClick = { showMissionDialog = true },
                                onLockedFeatureClick = { showPremiumDialog = true },
                                onQRCodeClick = { 
                                    if (isPremium) {
                                        showQRCodeSelection = true
                                    } else {
                                        showPremiumDialog = true
                                    }
                                }
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
        val initialTopicIds = remember(uiState.selectedTopicIds) {
            uiState.selectedTopicIds.filter { it.isSelected }.map { it.id }.toSet()
        }

        val initialQuestionIds = remember(uiState.selectedQuestions) {
            uiState.selectedQuestions.map { it.id }.toSet()
        }
        MissionSelectionDialog(
            initialCount = uiState.questionCount,
            initialSelectionIds = initialQuestionIds, // Truyền Set<Int>
            initialTopicIds = initialTopicIds,
            onDismiss = { showMissionDialog = false },
            onConfirm = { count, questions, topics ->
                // questions trả về ở đây là List<MissionQuestion> với ID kiểu Int
                viewModel.updateMission(count, questions, topics)
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
    
    if (showPremiumDialog) {
        PremiumPurchaseDialog(
            onDismiss = { showPremiumDialog = false },
            onPurchaseSuccess = { showPremiumDialog = false }
        )
    }
    
    if (showResetPremiumDialog) {
        ResetPremiumDialog(
            onDismiss = { showResetPremiumDialog = false },
            onConfirmReset = {
                premiumManager.resetPremiumForTesting()
                showResetPremiumDialog = false
            }
        )
    }
    
    // QR Code Selection Dialog
    if (showQRCodeSelection) {
        QRCodeSelectionDialog(
            alarmId = uiState.alarmId ?: 0,
            initialSelectedIds = uiState.selectedQRCodeIds,
            onDismiss = { showQRCodeSelection = false },
            onConfirm = { selectedIds ->
                viewModel.updateSelectedQRCodes(selectedIds)
                showQRCodeSelection = false
            },
            onManageQRCodes = {
                showQRCodeSelection = false
                showQRCodeManagement = true
            }
        )
    }
    
    // QR Code Management Dialog
    if (showQRCodeManagement) {
        QRCodeManagementDialog(
            onDismiss = { 
                showQRCodeManagement = false
                scannedCodeFromCamera = null // Clear scanned code when closing
            },
            onScanCamera = {
                showQRCodeManagement = false
                showQRScanner = true
            },
            scannedCodeFromCamera = scannedCodeFromCamera,
            onClearScannedCode = { scannedCodeFromCamera = null }
        )
    }
    
    // Full screen QR Scanner
    if (showQRScanner) {
        QRCodeScannerScreen(
            onCodeScanned = { code, type ->
                // Lưu code đã quét và quay về management dialog
                scannedCodeFromCamera = Pair(code, type)
                showQRScanner = false
                showQRCodeManagement = true
            },
            onClose = { 
                showQRScanner = false
                showQRCodeManagement = true
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

// Định nghĩa các tính năng nâng cao bị khóa
data class PremiumFeature(
    val id: Int,
    val name: String,
    val icon: @Composable () -> Unit
)

@Composable
fun AlarmTaskSection(
    questionCount: Int,
    isPremium: Boolean,
    onAddClick: () -> Unit,
    onLockedFeatureClick: () -> Unit,
    onQRCodeClick: () -> Unit
) {
    // Danh sách các tính năng nâng cao
    val premiumFeatures = remember {
        listOf(
            PremiumFeature(1, "Toán", { Text("🧮", fontSize = 20.sp) }),
            PremiumFeature(2, "Hình ảnh", { Text("🖼️", fontSize = 20.sp) }),
            PremiumFeature(3, "QR Code", { Text("📱", fontSize = 20.sp) })
        )
    }
    
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card đầu tiên: Thêm câu hỏi
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
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
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
        
        // Các tính năng Premium
        items(premiumFeatures.size) { index ->
            val feature = premiumFeatures[index]
            PremiumFeatureCard(
                feature = feature,
                isUnlocked = isPremium,
                onClick = {
                    if (isPremium) {
                        // Xử lý tính năng QR Code
                        if (feature.id == 3) {
                            onQRCodeClick()
                        }
                        // TODO: Xử lý các tính năng khác (Toán, Hình ảnh)
                    } else {
                        onLockedFeatureClick()
                    }
                }
            )
        }
    }
}

@Composable
private fun PremiumFeatureCard(
    feature: PremiumFeature,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val goldColor = Color(0xFFFFD700)
    
    Box(modifier = Modifier) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) 
                    goldColor.copy(alpha = 0.2f) 
                else 
                    MaterialTheme.colorScheme.tertiary
            ),
            onClick = onClick,
            modifier = Modifier
                .size(80.dp)
                .padding(top = 4.dp, end = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) 
                                goldColor.copy(alpha = 0.3f) 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        feature.icon()
                    } else {
                        Icon(
                            Icons.Default.Lock, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (isUnlocked) feature.name else "Khóa",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnlocked) 
                        goldColor 
                    else 
                        MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
        
        // Badge "PRO" nếu chưa mở khóa
        if (!isUnlocked) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 0.dp),
                shape = RoundedCornerShape(4.dp),
                color = goldColor
            ) {
                Text(
                    text = "PRO",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsSectionHeader(
    title: String,
    isPremium: Boolean = false,
    onLongPress: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onLongPress != null) {
                    Modifier.combinedClickable(
                        onClick = { },
                        onLongClick = onLongPress
                    )
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp
        )
        
        // Hiển thị badge Premium nếu đã mua
        if (isPremium) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.2f)
            ) {
                Text(
                    text = "⭐ PREMIUM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
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
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
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

/**
 * Dialog để reset trạng thái Premium (chỉ dành cho testing)
 */
@Composable
fun ResetPremiumDialog(
    onDismiss: () -> Unit,
    onConfirmReset: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.width(320.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔧",
                        fontSize = 28.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Reset Premium",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Bạn có chắc muốn reset trạng thái Premium? Thao tác này chỉ dành cho testing.",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ Chức năng ẩn dành cho Developer",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = onConfirmReset,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Reset", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}