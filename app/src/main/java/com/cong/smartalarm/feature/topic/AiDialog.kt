package com.cong.smartalarm.feature.topic

import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.core.designsystem.theme.Calm

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.feature.topic.model.AiMatrixConfig
import com.cong.smartalarm.feature.topic.model.SelectedDocument

/** Bottom sheet trợ lý AI: tải tài liệu, nén và cấu hình ma trận câu hỏi. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCreateQuestionBottomSheet(
    isLoading: Boolean,
    matrixConfig: AiMatrixConfig?,
    selectedDocuments: List<SelectedDocument>, // Lắng nghe danh sách hàng chờ truyền từ ngoài vào

    onDismiss: () -> Unit,
    onAddDocument: (Uri, String, Boolean) -> Unit,
    onRemoveDocument: (String) -> Unit,
    onUpdatePageConfig: (String, String) -> Unit,
    onProcessAndCompress: () -> Unit,
    onTopicNameChange: (String) -> Unit,
    onPresetSelect: (isTryHard: Boolean) -> Unit,
    onMatrixSliderChange: (easy: Float, mid: Float, hard: Float) -> Unit,
    onStartGenerationSuccess: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Bộ chọn tài liệu hệ thống đa phương thức đầu vào
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUri(context, it)
            val mimeType = context.contentResolver.getType(it)
            val isPdf = mimeType == "application/pdf" || fileName.endsWith(".pdf", ignoreCase = true)
            onAddDocument(it, fileName, isPdf)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Calm.Subtle, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trợ lý Tạo đề AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Phân luồng hiển thị dựa theo Kiến trúc trạng thái nâng cấp (Hoisted State)
            when {
                isLoading -> {
                    ProcessingOcrLayout()
                }
                matrixConfig == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        UploadZoneLayout(
                            isCompact = selectedDocuments.isNotEmpty(),
                            onUploadClick = { documentPickerLauncher.launch("*/*") }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render danh sách tài liệu đang nằm trong hàng chờ
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items = selectedDocuments, key = { it.id }) { doc ->
                                DocumentItemRow(
                                    document = doc,
                                    onRemoveClick = { onRemoveDocument(doc.id) },
                                    onPageConfigChange = { newConfig -> onUpdatePageConfig(doc.id, newConfig) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nút hành động chính để thực thi lưu trữ/nén tạm thời
                        CalmButton(
                            onClick = onProcessAndCompress,
                            enabled = selectedDocuments.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Compress, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gửi AI phân tích",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                else -> {
                    ConfigureMatrixLayout(
                        config = matrixConfig,
                        onTopicChange = onTopicNameChange,
                        onPresetSelect = onPresetSelect,
                        onSliderChange = onMatrixSliderChange,
                        onGenerateClick = {
                            onStartGenerationSuccess(0)
                        }
                    )
                }
            }
        }
    }
}

// ======================= CÁC THÀNH PHẦN GIAO DIỆN CON TÁCH BIỆT =======================

@Composable
private fun UploadZoneLayout(isCompact: Boolean, onUploadClick: () -> Unit) {
    CalmCard(
        onClick = onUploadClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 64.dp else 130.dp), // Tự động co giãn diện tích dựa theo trạng thái hàng chờ
        colors = CardDefaults.cardColors(containerColor = Calm.Raised, contentColor = Calm.Foreground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isCompact) Arrangement.Start else Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CloudUpload,
                contentDescription = "Upload",
                modifier = Modifier.size(if (isCompact) 24.dp else 34.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = if (isCompact) Alignment.Start else Alignment.CenterHorizontally
            ) {
                Text(text = "Tải thêm tài liệu", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (!isCompact) {
                    Text(
                        text = "Có thể tải nhiều file Ảnh và PDF cùng lúc",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProcessingOcrLayout() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Hệ thống đang chạy tiến trình nén phân nhánh ngầm...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) result = cursor.getString(index)
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "TaiLieuChon_${System.currentTimeMillis()}"
}