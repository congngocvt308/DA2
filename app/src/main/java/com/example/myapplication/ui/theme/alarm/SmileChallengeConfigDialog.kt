package com.example.myapplication.ui.theme.alarm

import android.net.Uri
import android.provider.OpenableColumns
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.myapplication.data.AlarmFaceMissionConfigEntity
import com.example.myapplication.utils.SoundPlayer
import com.example.myapplication.utils.VoiceRecorder
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

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

    var isRecording by remember { mutableStateOf(false) }
    var currentRecordingLabel by remember { mutableStateOf("") }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start recording if we have a label
            if (currentRecordingLabel.isNotEmpty()) {
                // Thêm rec_ và timestamp để phân biệt các bản thu khác nhau
                recorder.startRecording("rec_${currentRecordingLabel.replace(" ", "_")}_${System.currentTimeMillis()}")
                isRecording = true
            }
        } else {
            Toast.makeText(context, "Cần cấp quyền truy cập Micro để thu âm", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            backgroundImageUri = uri
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
        containerColor = MaterialTheme.colorScheme.surface,
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
                    Icon(Icons.Default.Close, "Close")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ){
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Thời gian cười: ${smileDuration.toInt()} giây")
                    }
                    Slider(
                        value = smileDuration,
                        onValueChange = { smileDuration = it },
                        valueRange = 3f..15f,
                        steps = 12,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("3s", style = MaterialTheme.typography.bodySmall)
                        Text("15s", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            // --- PHẦN THỜI GIAN CƯỜI ---

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Âm thanh:")
                        }

                        ExposedDropdownMenuBox(
                            expanded = isSoundDropdownExpanded,
                            onExpandedChange = { isSoundDropdownExpanded = !isSoundDropdownExpanded }
                        ) {
                            // Nút anchor thiết kế lại cho hiện đại hơn
                            Surface(
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(140.dp)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = soundMode, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Icon(
                                        imageVector = if (isSoundDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = isSoundDropdownExpanded,
                                onDismissRequest = { isSoundDropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(text = { Text("Giọng AI", fontSize = 14.sp) }, onClick = { soundMode = "Giọng AI"; isSoundDropdownExpanded = false })
                                DropdownMenuItem(text = { Text("Tùy chỉnh", fontSize = 14.sp) }, onClick = { soundMode = "Tùy chỉnh"; isSoundDropdownExpanded = false })
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = soundMode == "Tùy chỉnh",
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Đường kẻ phân cách
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Thay đổi nhãn thành "Nhắc nhở" như thảo luận trước
                            SoundConfigRow(
                                icon = Icons.Default.Notifications, 
                                label = "(Nhắc nhở)",
                                hasFile = reminderSoundUri != null,
                                isPlaying = currentPlayingLabel == "Nhắc nhở",
                                onPlayClick = { playSound("Nhắc nhở", reminderSoundUri) },
                                onBrowseClick = {
                                    currentPickingLabel = "Nhắc nhở"
                                    showSoundPicker = true
                                },
                                onResetClick = { reminderSoundUri = null }
                            )
                            SoundConfigRow(
                                icon = Icons.Default.Visibility, 
                                label = "(Mở mắt)",
                                hasFile = eyeOpenSoundUri != null,
                                isPlaying = currentPlayingLabel == "Mở mắt",
                                onPlayClick = { playSound("Mở mắt", eyeOpenSoundUri) },
                                onBrowseClick = {
                                    currentPickingLabel = "Mở mắt"
                                    showSoundPicker = true
                                },
                                onResetClick = { eyeOpenSoundUri = null }
                            )
                            SoundConfigRow(
                                icon = Icons.Default.SentimentSatisfiedAlt, 
                                label = "(Cười lên)",
                                hasFile = smileSoundUri != null,
                                isPlaying = currentPlayingLabel == "Cười lên",
                                onPlayClick = { playSound("Cười lên", smileSoundUri) },
                                onBrowseClick = {
                                    currentPickingLabel = "Cười lên"
                                    showSoundPicker = true
                                },
                                onResetClick = { smileSoundUri = null }
                            )
                            SoundConfigRow(
                                icon = Icons.Default.HourglassEmpty, 
                                label = "(Giữ nguyên)",
                                hasFile = holdPoseSoundUri != null,
                                isPlaying = currentPlayingLabel == "Giữ nguyên",
                                onPlayClick = { playSound("Giữ nguyên", holdPoseSoundUri) },
                                onBrowseClick = {
                                    currentPickingLabel = "Giữ nguyên"
                                    showSoundPicker = true
                                },
                                onResetClick = { holdPoseSoundUri = null }
                            )
                        }
                    }
                }
            }

            // --- PHẦN ÂM THANH ---


            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { imagePickerLauncher.launch("image/*") }
            ){
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hình nền:", modifier = Modifier.weight(1f))

                    AsyncImage(
                        model = backgroundImageUri ?: defaultBgUri,
                        contentDescription = "Background Preview",
                        modifier = Modifier
                            .width(52.dp)
                            .aspectRatio(9f / 19.5f) // Tỷ lệ màn hình điện thoại chuẩn
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
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

    if (isRecording) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "recording")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.width(280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đang thu âm...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mục: $currentRecordingLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.1f))
                            .border(2.dp, Color.Red, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp).scale(scale)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            val path = recorder.stopRecording()
                            isRecording = false
                            if (path != null) {
                                val duration = getAudioDuration(path)
                                if (duration > 4000) {
                                    // Xóa file ngay lập tức nếu quá dài
                                    File(path).delete()
                                    Toast.makeText(context, "Bản thu quá dài (vượt quá 4s). Vui lòng thu âm ngắn hơn!", Toast.LENGTH_LONG).show()
                                } else {
                                    // CẬP NHẬT ĐƯỜNG DẪN VÀO ĐÚNG MỤC
                                    when (currentRecordingLabel) {
                                        "Nhắc nhở" -> reminderSoundUri = path
                                        "Mở mắt" -> eyeOpenSoundUri = path
                                        "Cười lên" -> smileSoundUri = path
                                        "Giữ nguyên" -> holdPoseSoundUri = path
                                    }
                                    Toast.makeText(context, "Đã lưu bản thu cho $currentRecordingLabel!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Stop, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dừng & Lưu")
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundConfigRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    label: String,
    hasFile: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onBrowseClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onPlayClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, 
                    contentDescription = null, 
                    modifier = Modifier.size(18.dp), 
                    tint = if (hasFile) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onBrowseClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onResetClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun SoundPickerDialog(
    onDismiss: () -> Unit,
    onRecordNew: () -> Unit,
    onPickFromFile: () -> Unit,
    onSoundSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var filesList by remember { 
        mutableStateOf(
            java.io.File(context.filesDir.absolutePath).listFiles { file -> 
                file.extension == "m4a" || file.extension == "mp3" 
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thư viện âm thanh", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "* Lưu ý: File âm thanh cần dưới 4 giây",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onRecordNew, 
                        modifier = Modifier.weight(0.9f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Mic, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thu âm", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onPickFromFile, 
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chọn tệp", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Bản thu trước đó:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                    if (filesList.isEmpty()) {
                        item { 
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                Text("Chưa có bản thu nào", color = Color.Gray, fontSize = 14.sp) 
                            }
                        }
                    }
                    items(filesList) { file ->
                        Surface(
                            onClick = { onSoundSelected(file.absolutePath) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Audiotrack, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    file.name, 
                                    modifier = Modifier.weight(1f), 
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (file.delete()) {
                                            filesList = filesList.filter { it.absolutePath != file.absolutePath }
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close, 
                                        contentDescription = "Xóa file", 
                                        modifier = Modifier.size(16.dp), 
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hàm hỗ trợ sao chép tệp từ Uri vào bộ nhớ trong của ứng dụng
 */
private fun copyUriToInternalStorage(context: android.content.Context, uri: Uri): String? {
    val contentResolver = context.contentResolver
    // Đảm bảo tên file copy vào luôn là duy nhất bằng cách chèn timestamp vào trước tên gốc
    val originalName = getFileName(context, uri) ?: "sound"
    val fileName = "file_${System.currentTimeMillis()}_$originalName"
    val outputFile = File(context.filesDir, fileName)

    return try {
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(outputFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
    }
    if (name == null) {
        name = uri.path
        val cut = name?.lastIndexOf('/') ?: -1
        if (cut != -1) name = name?.substring(cut + 1)
    }
    return name
}

private fun getAudioDuration(context: android.content.Context, uri: Uri): Long {
    val retriever = android.media.MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
        time?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}

// Overload cho đường dẫn file string
private fun getAudioDuration(path: String): Long {
    val retriever = android.media.MediaMetadataRetriever()
    return try {
        retriever.setDataSource(path)
        val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
        time?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}
