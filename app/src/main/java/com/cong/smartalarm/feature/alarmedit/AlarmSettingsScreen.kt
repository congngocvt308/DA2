package com.cong.smartalarm.feature.alarmedit
import com.cong.smartalarm.core.designsystem.component.CalmSlider
import com.cong.smartalarm.core.designsystem.component.CalmSwitch

import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.core.designsystem.component.CalmDialogSurface
import com.cong.smartalarm.core.designsystem.component.SoftSurface
import com.cong.smartalarm.core.designsystem.component.SoftSurfaceButton
import com.cong.smartalarm.feature.smile.FaceMissionScreen
import com.cong.smartalarm.feature.smile.FaceMissionViewModel
import com.cong.smartalarm.feature.smile.FaceMissionViewModelFactory
import com.cong.smartalarm.feature.mission.MissionSelectionDialog
import com.cong.smartalarm.feature.qr.QRCodeManagementDialog
import com.cong.smartalarm.feature.qr.QRCodeScannerScreen
import com.cong.smartalarm.feature.qr.QRCodeSelectionDialog
import com.cong.smartalarm.core.media.RingtoneUtils
import com.cong.smartalarm.core.media.SoundPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
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
            factory = FaceMissionViewModelFactory(context, uiState.id)
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
                    SoftSurface(
                        cornerRadius = 22.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = uiState.label,
                            onValueChange = { viewModel.onLabelChanged(it) },
                            label = { Text("Tên báo thức", color = Calm.MutedForeground) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Calm.Foreground,
                                unfocusedTextColor = Calm.Foreground,
                                cursorColor = Calm.Primary
                            ),
                            leadingIcon = {
                                Image(
                                    painter = painterResource(com.cong.smartalarm.R.drawable.sun),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Sửa tên",
                                    tint = Calm.MutedForeground
                                )
                            },
                        )
                    }
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

@Composable
fun MissionTypeSelectionDialog(
    onDismiss: () -> Unit,
    onQuizSelected: () -> Unit,
    onSmileChallengeSelected: () -> Unit,
    onQRCodeSelected: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CalmDialogSurface(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chọn loại nhiệm vụ",
                            color = Calm.Foreground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hoàn thành nhiệm vụ để tắt chuông",
                            color = Calm.MutedForeground,
                            fontSize = 13.sp
                        )
                    }
                    // Nút đóng tròn nhỏ
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Calm.NeutralMist)
                            .border(1.dp, Calm.NeutralSoft, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Calm.Foreground, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                MissionTypeItem(
                    title = "Giải trắc nghiệm",
                    description = "Giải các câu hỏi để tỉnh táo.",
                    icon = "🧮",
                    accent = listOf(Calm.NeutralMist, Calm.NeutralMist),
                    onClick = onQuizSelected
                )
                Spacer(modifier = Modifier.height(10.dp))
                MissionTypeItem(
                    title = "Thử thách nụ cười",
                    description = "Mở mắt, cười thật tươi và giữ tư thế để xác nhận thức dậy.",
                    icon = "😊",
                    accent = listOf(Calm.WarmMist, Calm.WarmMist),
                    onClick = onSmileChallengeSelected
                )
                Spacer(modifier = Modifier.height(10.dp))
                MissionTypeItem(
                    title = "Quét mã QR/Barcode",
                    description = "Rời khỏi giường và quét mã để tắt chuông.",
                    icon = Icons.Default.QrCodeScanner,
                    accent = listOf(Calm.WellnessMist, Calm.WellnessMist),
                    onClick = onQRCodeSelected
                )
            }
        }
    }
}

@Composable
private fun MissionTypeItem(
    title: String,
    description: String,
    icon: Any,
    accent: List<Color>,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Calm.Surface)
            .border(1.dp, Calm.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon trong "giọt" gradient màu riêng của từng nhiệm vụ
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(accent)),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is String -> Text(text = icon, fontSize = 22.sp)
                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Calm.WellnessText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Calm.Foreground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Calm.MutedForeground
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Calm.MutedForeground
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
        CalmSlider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.weight(1f)
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


/** Thẻ nổi mềm bo 28dp cho từng nhóm cài đặt. */
@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    SoftSurface(
        cornerRadius = 28.dp,
        contentAlignment = Alignment.TopStart,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), content = content)
    }
}

/** Hàng bấm được bên trong thẻ cài đặt (nền xám rất nhạt). */
@Composable
private fun SettingsRow(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Calm.NeutralMist)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun SoundSettingsSection(
    currentSoundTitle: String,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    onSoundClick: () -> Unit
) {
    SettingsSection {
        SettingsSectionHeader(title = "Âm thanh báo thức")
        Spacer(modifier = Modifier.height(12.dp))

        // Dòng tên bài hát
        SettingsRow(onClick = onSoundClick) {
            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Calm.Primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = currentSoundTitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Calm.Foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Calm.MutedForeground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dòng Slider Volume
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Âm lượng", tint = Calm.MutedForeground)
            Spacer(modifier = Modifier.width(12.dp))
            CalmSlider(
                value = currentVolume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AlarmTaskSection(
    questionCount: Int,
    faceMissionConfig: com.cong.smartalarm.core.database.entity.AlarmFaceMissionConfigEntity?,
    selectedQRCodeIds: List<Int>,
    onAddClick: () -> Unit,
    onClearClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val hasQuiz = questionCount > 0
    val hasSmile = faceMissionConfig != null
    val hasQR = selectedQRCodeIds.isNotEmpty()
    val hasAnyMission = hasQuiz || hasSmile || hasQR
    val tileShape = RoundedCornerShape(18.dp)

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. CARD NHIỆM VỤ CHÍNH (80x80) ---
        item {
            Box(modifier = Modifier.padding(top = 8.dp, end = 8.dp)) {
                Column(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(tileShape)
                        .background(if (hasAnyMission) Calm.NeutralMist else Calm.Muted)
                        .border(1.dp, Calm.NeutralSoft, tileShape)
                        .clickable(onClick = if (hasAnyMission) onEditClick else onAddClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (hasAnyMission) Calm.Surface else Calm.NeutralMist),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            hasSmile -> Text("😊", fontSize = 18.sp)
                            hasQuiz -> Text("🧮", fontSize = 18.sp)
                            hasQR -> Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Calm.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            else -> Icon(
                                Icons.Default.Add,
                                contentDescription = "Thêm nhiệm vụ",
                                tint = Calm.Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            hasSmile -> "${faceMissionConfig?.requiredHoldDurationSec} giây"
                            hasQuiz -> "$questionCount câu"
                            hasQR -> "${selectedQRCodeIds.size} mã"
                            else -> "Thêm"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Calm.Foreground,
                        fontSize = 11.sp
                    )
                }

                // NÚT XÓA NHIỆM VỤ (Dấu X đỏ)
                if (hasAnyMission) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .offset(x = 4.dp, y = (-4).dp)
                            .clickable { onClearClick() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Xóa nhiệm vụ",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }

        // --- 2. BA CARD KHÓA (PLACEHOLDERS CHO TƯƠNG LAI) ---
        items(3) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(tileShape)
                    .background(Calm.Muted)
                    .border(1.dp, Calm.Border, tileShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Coming Soon",
                    tint = Calm.MutedForeground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/** Index giữa danh sách "vô hạn" ứng với [value] (chia hết cho [count] để index % count == value). */
private fun wheelStartIndex(value: Int, count: Int): Int {
    val base = Int.MAX_VALUE / 2
    return base - (base % count) + value
}

private val WheelItemHeight = 48.dp
private val WheelViewportHeight = 150.dp

@Composable
fun TimePickerSection(
    hourListState: LazyListState,
    minuteListState: LazyListState,
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
){
    val scope = rememberCoroutineScope()
    // Index tuyệt đối lúc bánh xe dừng lần trước: so "vòng" (index / 60) để biết phút đã qua mốc 59 <-> 00
    var settledHourIndex by remember { mutableStateOf(hourListState.firstVisibleItemIndex) }
    var settledMinuteIndex by remember { mutableStateOf(minuteListState.firstVisibleItemIndex) }

    RememberInfiniteSnap(hourListState) { index ->
        settledHourIndex = index
        onHourChange(index % 24)
    }
    RememberInfiniteSnap(minuteListState) { index ->
        val laps = Math.floorDiv(index, 60) - Math.floorDiv(settledMinuteIndex, 60)
        settledMinuteIndex = index
        onMinuteChange(index % 60)
        // Kéo lên qua 59 -> 00: giờ +1; kéo xuống qua 00 -> 59: giờ -1 (vuốt mạnh nhiều vòng thì nhiều giờ)
        if (laps != 0) {
            val hourIndex = settledHourIndex + laps
            settledHourIndex = hourIndex
            onHourChange(Math.floorMod(hourIndex, 24))
            scope.launch { hourListState.animateScrollToItem(hourIndex) }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WheelViewportHeight + 40.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dải chọn ở giữa, kiểu bánh xe iOS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Calm.NeutralMist)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(WheelViewportHeight)
                // Mờ dần hai mép trên/dưới
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.35f to Color.Black,
                            0.65f to Color.Black,
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfiniteWheelColumn(
                state = hourListState,
                itemCount = 24,
                selectedItem = selectedHour,
                alignment = Alignment.End
            )

            // Dấu ":" nằm trong ô cao bằng ô số, cùng kiểu chữ -> thẳng hàng với số đang chọn
            Box(
                modifier = Modifier
                    .height(WheelItemHeight)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "lnum, tnum, case"),
                    color = Calm.Foreground,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            InfiniteWheelColumn(
                state = minuteListState,
                itemCount = 60,
                selectedItem = selectedMinute,
                alignment = Alignment.Start
            )
        }
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
        modifier = Modifier
            .wrapContentWidth()
            .height(WheelViewportHeight),
        horizontalAlignment = alignment,
        // Đệm đúng nửa phần thừa -> mục ở vị trí cuộn nằm chính giữa khung
        contentPadding = PaddingValues(vertical = (WheelViewportHeight - WheelItemHeight) / 2)
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

/**
 * Khi bánh xe dừng: hút mục gần tâm nhất về giữa rồi báo index tuyệt đối của nó.
 * Toạ độ item tính từ sau contentPadding, nên tâm thật = (viewportStartOffset + viewportEndOffset) / 2.
 */
@Composable
private fun RememberInfiniteSnap(
    lazyListState: LazyListState,
    onItemSettled: (index: Int) -> Unit
) {
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            delay(50)
            val layoutInfo = lazyListState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) return@LaunchedEffect
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val closestItem = layoutInfo.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - center)
            } ?: return@LaunchedEffect
            onItemSettled(closestItem.index)
            lazyListState.animateScrollToItem(closestItem.index)
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
    val dayOrder = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRepeatDailyChange(!isDaily) }
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hàng ngày",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Calm.Foreground
            )
            CalmSwitch(
                checked = isDaily,
                onCheckedChange = { onRepeatDailyChange(it) }
            )
        }

        // 7 chip chia đều bề ngang: 7 × 44dp cố định bị tràn trên máy hẹp nên chip "CN" bị bóp nhỏ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dayOrder.forEach { day ->
                CustomDayChip(
                    text = day,
                    isSelected = daysOfWeek.contains(day),
                    onClick = { onDayToggle(day) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomDayChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (isSelected) Calm.Primary else Calm.NeutralMist)
            .border(1.dp, if (isSelected) Calm.Primary else Calm.NeutralSoft, shape)
            .selectable(selected = isSelected, role = Role.Checkbox, onClick = onClick)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Calm.Foreground,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsSectionHeader(
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Calm.Foreground,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun TimePickerItem(
    text: String,
    isSelected: Boolean
) {
    Box(Modifier.height(WheelItemHeight), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = if (isSelected) 36.sp else 32.sp,
            color = if (isSelected) Calm.Foreground else Calm.MutedForeground,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SnoozeSettingsSection(
    isSnoozeEnabled: Boolean,
    snoozeDuration: Int,
    onSnoozeToggle: (Boolean) -> Unit,
    onDurationClick: () -> Unit
) {
    SettingsSection {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Báo lại",
                color = Calm.Foreground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            CalmSwitch(
                checked = isSnoozeEnabled,
                onCheckedChange = onSnoozeToggle
            )
        }

        AnimatedVisibility(
            visible = isSnoozeEnabled,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                SettingsRow(onClick = onDurationClick) {
                    Text(
                        text = "Khoảng thời gian",
                        color = Calm.Foreground,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$snoozeDuration phút",
                        color = Calm.Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Calm.Primary,
                        modifier = Modifier.size(14.dp)
                    )
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
        CalmCard(
            shape = RoundedCornerShape(28.dp),
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
                                .background(if (isSelected) Calm.NeutralMist else Color.Transparent)
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$duration phút",
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

