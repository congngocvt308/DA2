package com.cong.smartalarm.feature.alarmedit

import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.database.entity.AlarmFaceMissionConfigEntity
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.core.media.SoundPlayer
import com.cong.smartalarm.core.media.VoiceRecorder
import com.cong.smartalarm.feature.alarmedit.smile.BackgroundEditorDialog
import com.cong.smartalarm.feature.alarmedit.smile.BackgroundPreviewCard
import com.cong.smartalarm.feature.alarmedit.smile.BackgroundSourcePickerDialog
import com.cong.smartalarm.feature.alarmedit.smile.SmileDurationCard
import com.cong.smartalarm.feature.alarmedit.smile.SmileSoundSection
import com.cong.smartalarm.feature.alarmedit.smile.SoundPickerDialog
import com.cong.smartalarm.feature.alarmedit.smile.VoiceRecordingDialog
import com.cong.smartalarm.feature.alarmedit.smile.copyUriToInternalStorage
import com.cong.smartalarm.feature.alarmedit.smile.getAudioDuration
import java.io.File
import java.util.Locale

/** Bottom sheet cấu hình thử thách nụ cười: thời gian cười, âm thanh, hình nền. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmileChallengeConfigDialog(
    initialConfig: AlarmFaceMissionConfigEntity? = null,
    onDismiss: () -> Unit,
    onTest: () -> Unit,
    onSave: (AlarmFaceMissionConfigEntity) -> Unit
) {
    val context = LocalContext.current
    val defaultBgUri = remember { Uri.parse("android.resource://${context.packageName}/drawable/background") }

    var smileDuration by remember { mutableStateOf(initialConfig?.requiredHoldDurationSec?.toFloat() ?: 5f) }
    var isSoundDropdownExpanded by remember { mutableStateOf(false) }
    var soundMode by remember {
        mutableStateOf(
            if (initialConfig?.eyeOpenSoundUri == null &&
                initialConfig?.smileSoundUri == null &&
                initialConfig?.comboSoundUri == null &&
                initialConfig?.holdPoseSoundUri == null) "Giọng AI" else "Tùy chỉnh"
        )
    }
    var backgroundImageUri by remember {
        mutableStateOf<Uri?>(initialConfig?.backgroundImageUri?.let { Uri.parse(it) } ?: defaultBgUri)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var reminderSoundUri by remember { mutableStateOf<String?>(initialConfig?.comboSoundUri) }
    var eyeOpenSoundUri by remember { mutableStateOf<String?>(initialConfig?.eyeOpenSoundUri) }
    var smileSoundUri by remember { mutableStateOf<String?>(initialConfig?.smileSoundUri) }
    var holdPoseSoundUri by remember { mutableStateOf<String?>(initialConfig?.holdPoseSoundUri) }

    // State quản lý Dialog chọn âm thanh
    var showSoundPicker by remember { mutableStateOf(false) }
    var currentPickingLabel by remember { mutableStateOf("") }

    // State quản lý hình nền mới
    var showBackgroundSourcePicker by remember { mutableStateOf(false) }
    var showBackgroundEditor by remember { mutableStateOf(false) }
    var tempImageUriForEditor by remember { mutableStateOf<Uri?>(null) }

    val recorder = remember { VoiceRecorder(context) }
    val soundPlayer = remember { SoundPlayer(context) }
    var currentPlayingLabel by remember { mutableStateOf<String?>(null) }

    // TextToSpeech cho giọng AI
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("vi", "VN")

                // Thêm listener để reset icon khi đọc xong
                ttsInstance?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        if (currentPlayingLabel == utteranceId) {
                            currentPlayingLabel = null
                        }
                    }
                    override fun onError(utteranceId: String?) {
                        if (currentPlayingLabel == utteranceId) {
                            currentPlayingLabel = null
                        }
                    }
                })
            }
        }
        ttsInstance
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.stop()
            tts?.stop()
            tts?.shutdown()
        }
    }

    fun playSound(label: String, uri: String?) {
        if (currentPlayingLabel == label) {
            soundPlayer.stop()
            tts?.stop()
            currentPlayingLabel = null
            return
        }

        soundPlayer.stop()
        tts?.stop()
        currentPlayingLabel = label

        if (uri != null) {
            soundPlayer.playOrUpdateVolume(uri, 1.0f, onCompletion = {
                currentPlayingLabel = null
            })
        } else {
            val text = when (label) {
                "Nhắc nhở" -> "Mở mắt và cười lên nào! "
                "Mở mắt" -> "Mở mắt ra nào! "
                "Cười lên" -> "Cười tươi lên nào!"
                "Giữ nguyên" -> "Giữ nguyên nhé!"
                else -> ""
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, label)
        }
    }

    var showRecordingDialog by remember { mutableStateOf(false) }
    var isActuallyRecording by remember { mutableStateOf(false) }
    var currentRecordingLabel by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (currentRecordingLabel.isNotEmpty()) {
                showRecordingDialog = true
                isActuallyRecording = false
            }
        } else {
            Toast.makeText(context, "Cần cấp quyền truy cập Micro để thu âm", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            tempImageUriForEditor = uri
            showBackgroundEditor = true
        }
    }

    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val duration = getAudioDuration(context, it)
            if (duration > 4000) { // 4000ms = 4s
                Toast.makeText(context, "Lỗi: Tệp âm thanh quá dài (vượt quá 4s)!", Toast.LENGTH_LONG).show()
            } else {
                val copiedPath = copyUriToInternalStorage(context, it)
                if (copiedPath != null) {
                    when (currentPickingLabel) {
                        "Nhắc nhở" -> reminderSoundUri = copiedPath
                        "Mở mắt" -> eyeOpenSoundUri = copiedPath
                        "Cười lên" -> smileSoundUri = copiedPath
                        "Giữ nguyên" -> holdPoseSoundUri = copiedPath
                    }
                    Toast.makeText(context, "Đã nhập tệp thành công!", Toast.LENGTH_SHORT).show()
                }
            }
            showSoundPicker = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Calm.Background,
        contentColor = Calm.Foreground,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            // 2. Đây là cái thanh gạch, hệ thống sẽ tự vẽ và cho phép KÉO
            BottomSheetDefaults.DragHandle()
        }
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Tránh bị đè bởi phím hệ thống
                .padding(start = 24.dp, end = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thử thách nụ cười",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Đóng", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- PHẦN THỜI GIAN CƯỜI ---
            SmileDurationCard(
                smileDuration = smileDuration,
                onDurationChange = { smileDuration = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- PHẦN ÂM THANH ---
            SmileSoundSection(
                soundMode = soundMode,
                isDropdownExpanded = isSoundDropdownExpanded,
                onDropdownExpandedChange = { isSoundDropdownExpanded = it },
                onSoundModeChange = { soundMode = it },
                reminderSoundUri = reminderSoundUri,
                eyeOpenSoundUri = eyeOpenSoundUri,
                smileSoundUri = smileSoundUri,
                holdPoseSoundUri = holdPoseSoundUri,
                currentPlayingLabel = currentPlayingLabel,
                onPlay = { label, uri -> playSound(label, uri) },
                onBrowse = { label ->
                    currentPickingLabel = label
                    showSoundPicker = true
                },
                onReset = { label ->
                    when (label) {
                        "Nhắc nhở" -> reminderSoundUri = null
                        "Mở mắt" -> eyeOpenSoundUri = null
                        "Cười lên" -> smileSoundUri = null
                        "Giữ nguyên" -> holdPoseSoundUri = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- PHẦN HÌNH NỀN ---
            BackgroundPreviewCard(
                imageUri = backgroundImageUri ?: defaultBgUri,
                onClick = { showBackgroundSourcePicker = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            CalmButton(
                onClick = {
                    val config = AlarmFaceMissionConfigEntity(
                        alarmId = initialConfig?.alarmId ?: 0,
                        backgroundImageUri = backgroundImageUri?.toString(),
                        eyeOpenSoundUri = if (soundMode == "Tùy chỉnh") eyeOpenSoundUri else null,
                        smileSoundUri = if (soundMode == "Tùy chỉnh") smileSoundUri else null,
                        comboSoundUri = if (soundMode == "Tùy chỉnh") reminderSoundUri else null,
                        holdPoseSoundUri = if (soundMode == "Tùy chỉnh") holdPoseSoundUri else null,
                        requiredHoldDurationSec = smileDuration.toInt()
                    )
                    onSave(config)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Hoàn tất", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if (showSoundPicker) {
        SoundPickerDialog(
            onDismiss = { showSoundPicker = false },
            onRecordNew = {
                showSoundPicker = false
                currentRecordingLabel = currentPickingLabel
                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            },
            onPickFromFile = {
                audioFilePickerLauncher.launch("audio/*")
            },
            onSoundSelected = { path ->
                when (currentPickingLabel) {
                    "Nhắc nhở" -> reminderSoundUri = path
                    "Mở mắt" -> eyeOpenSoundUri = path
                    "Cười lên" -> smileSoundUri = path
                    "Giữ nguyên" -> holdPoseSoundUri = path
                }
                showSoundPicker = false
            }
        )
    }

    if (showBackgroundSourcePicker) {
        BackgroundSourcePickerDialog(
            onDismiss = { showBackgroundSourcePicker = false },
            onDefaultSelected = {
                backgroundImageUri = defaultBgUri
                showBackgroundSourcePicker = false
            },
            onGallerySelected = {
                showBackgroundSourcePicker = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }

    if (showBackgroundEditor && tempImageUriForEditor != null) {
        BackgroundEditorDialog(
            imageUri = tempImageUriForEditor!!,
            onDismiss = { showBackgroundEditor = false },
            onConfirm = { croppedPath ->
                backgroundImageUri = Uri.fromFile(File(croppedPath))
                showBackgroundEditor = false
            }
        )
    }

    if (showRecordingDialog) {
        VoiceRecordingDialog(
            label = currentRecordingLabel,
            isRecording = isActuallyRecording,
            onDismiss = { showRecordingDialog = false },
            onStart = {
                recorder.startRecording("rec_${currentRecordingLabel.replace(" ", "_")}_${System.currentTimeMillis()}")
                isActuallyRecording = true
            },
            onStopAndSave = {
                val path = recorder.stopRecording()
                isActuallyRecording = false
                showRecordingDialog = false
                if (path != null) {
                    val duration = getAudioDuration(path)
                    if (duration > 4000) {
                        File(path).delete()
                        Toast.makeText(context, "Bản thu quá dài (vượt quá 4s). Vui lòng thu âm ngắn hơn!", Toast.LENGTH_LONG).show()
                    } else {
                        when (currentRecordingLabel) {
                            "Nhắc nhở" -> reminderSoundUri = path
                            "Mở mắt" -> eyeOpenSoundUri = path
                            "Cười lên" -> smileSoundUri = path
                            "Giữ nguyên" -> holdPoseSoundUri = path
                        }
                        Toast.makeText(context, "Đã lưu bản thu cho $currentRecordingLabel!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
