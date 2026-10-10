package com.cong.smartalarm.feature.alarmedit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.core.designsystem.component.SoftSurfaceButton
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.core.media.RingtoneUtils
import com.cong.smartalarm.core.media.SoundPlayer
import com.cong.smartalarm.feature.mission.MissionSelectionDialog
import com.cong.smartalarm.feature.qr.QRCodeManagementDialog
import com.cong.smartalarm.feature.qr.QRCodeScannerScreen
import com.cong.smartalarm.feature.qr.QRCodeSelectionDialog
import com.cong.smartalarm.feature.smile.FaceMissionScreen
import com.cong.smartalarm.feature.smile.FaceMissionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(
    viewModel: AlarmSettingsViewModel = viewModel(factory = AlarmSettingsViewModel.Factory),
    onBackClick: () -> Unit,
    onMissionSettingClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Calm.Foreground)
        }
        return
    }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = wheelStartIndex(uiState.hour, 24))
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = wheelStartIndex(uiState.minute, 60))
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showMissionTypeDialog by remember { mutableStateOf(false) } // Mới: Hộp thoại chọn loại nhiệm vụ
    var showMissionDialog by remember { mutableStateOf(false) }
    var showSmileConfigDialog by remember { mutableStateOf(false) } // Mới: Hộp thoại nụ cười (thiết kế sau)
    var showSoundDialog by remember { mutableStateOf(false) }
    var showQRCodeManagement by remember { mutableStateOf(false) }
    var showQRCodeSelection by remember { mutableStateOf(false) }
    var showQRScanner by remember { mutableStateOf(false) }
    var showFaceMissionTest by remember { mutableStateOf(false) }

    // State để lưu code từ camera scanner
    var scannedCodeFromCamera by remember { mutableStateOf<Pair<String, String>?>(null) }

    val previewPlayer = remember { SoundPlayer(context) }

    val ringtoneTitle = remember(uiState.ringtoneUri) {
        RingtoneUtils.getRingtoneTitle(context, uiState.ringtoneUri)
    }

    if (showFaceMissionTest) {
        // Gọi ViewModel tương ứng của ông
        val faceViewModel: FaceMissionViewModel = viewModel(
            factory = FaceMissionViewModel.factory(uiState.id)
        )

        FaceMissionScreen(
            viewModel = faceViewModel,
            onLuongAComplete = {
                showFaceMissionTest = false
                android.widget.Toast.makeText(context, "Hoàn thành Luồng A! 🎉", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
        return // Dừng vẽ các UI phía dưới khi đang bật camera test
    }

    DisposableEffect(Unit) {
        onDispose { previewPlayer.stop() }
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
    ){
        Column(modifier = Modifier.fillMaxSize()) {
            // --- THANH TIÊU ĐỀ: NÚT KÍNH TRÒN + TIÊU ĐỀ ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SoftSurfaceButton(
                    onClick = { showDiscardDialog = true },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Close, "Đóng", tint = Calm.Foreground)
                }
                Text(
                    text = "Chuông báo thức",
                    color = Calm.Foreground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(44.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp)
            ){
                item {
                    AlarmLabelField(
                        label = uiState.label,
                        onLabelChange = { viewModel.onLabelChanged(it) }
                    )
                }

                // --- 1. PHẦN CHỈNH THỜI GIAN (2 LazyColumn) + 2. ĐỔ CHUÔNG SAU ---
                item {
                    SettingsSection {
                        TimePickerSection(
                            hourListState = hourListState,
                            minuteListState = minuteListState,
                            selectedHour = uiState.hour,
                            selectedMinute = uiState.minute,
                            onHourChange = { newHour -> viewModel.updateHour(newHour) },
                            onMinuteChange = { newMinute -> viewModel.updateMinute(newMinute) }
                        )
                        Text(
                            text = uiState.timeUntilAlarm,
                            color = Calm.MutedForeground,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                //--- 3. CHỌN NGÀY ---
                item {
                    SettingsSection {
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
                }

                // --- 4. NHIỆM VỤ BÁO THỨC ---
                item {
                    SettingsSection {
                        SettingsSectionHeader(
                            title = "Nhiệm vụ báo thức"
                        )
                        AlarmTaskSection(
                            questionCount = uiState.questionCount,
                            faceMissionConfig = uiState.faceMissionConfig,
                            selectedQRCodeIds = uiState.selectedQRCodeIds,
                            onAddClick = { showMissionTypeDialog = true },
                            onClearClick = { viewModel.clearMission() },
                            onEditClick = {
                                when {
                                    uiState.faceMissionConfig != null -> showSmileConfigDialog = true
                                    uiState.questionCount > 0 -> showMissionDialog = true
                                    uiState.selectedQRCodeIds.isNotEmpty() -> showQRCodeSelection = true
                                    else -> showMissionTypeDialog = true
                                }
                            }
                        )
                    }
                }

                // --- 5. ÂM THANH BÁO THỨC ---
                item {
                    SoundSettingsSection(
                        currentSoundTitle = ringtoneTitle,
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
        // --- NÚT LƯU: KÍNH NHUỘM MÀU NỔI ---
        SoftSurfaceButton(
            onClick = { viewModel.saveAlarm() },
            tint = SolidColor(Calm.Primary),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text("Lưu", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

    // --- DIALOG CHỌN LOẠI NHIỆM VỤ ---
    if (showMissionTypeDialog) {
        MissionTypeSelectionDialog(
            onDismiss = { showMissionTypeDialog = false },
            onQuizSelected = {
                showMissionTypeDialog = false
                showMissionDialog = true
            },
            onSmileChallengeSelected = {
                showMissionTypeDialog = false
                showSmileConfigDialog = true
            },
            onQRCodeSelected = {
                showMissionTypeDialog = false
                showQRCodeSelection = true
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

    // BẮT TRẠNG THÁI HIỂN THỊ MÀN HÌNH TEST CAMERA


    if (showSmileConfigDialog) {
        SmileChallengeConfigDialog(
            initialConfig = uiState.faceMissionConfig,
            onDismiss = { showSmileConfigDialog = false },
            onTest = {
                showSmileConfigDialog = false
                showFaceMissionTest = true // Mở camera test
            },
            onSave = { config ->
                viewModel.updateFaceMissionConfig(config)
                showSmileConfigDialog = false
            }
        )
    }
}
