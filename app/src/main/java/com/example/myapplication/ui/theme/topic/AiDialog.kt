package com.example.myapplication.ui.theme.topic

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AiMatrixConfig
import com.example.myapplication.data.SelectedDocument

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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
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
                        Button(
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
    Card(
        onClick = onUploadClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isCompact) 64.dp else 130.dp), // Tự động co giãn diện tích dựa theo trạng thái hàng chờ
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                tint = MaterialTheme.colorScheme.secondary
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
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentItemRow(
    document: SelectedDocument,
    onRemoveClick: () -> Unit,
    onPageConfigChange: (String) -> Unit
) {
    // Trạng thái đóng/mở khoang cấu hình chính của thẻ PDF
    var isExpanded by remember { mutableStateOf(false) }

    // Trạng thái đóng/mở Menu thả xuống chọn chế độ (Tất cả / Chọn khoảng)
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Chế độ đang chọn hiện tại (Tự động nhận diện dựa trên pageConfig hiện tại)
    val selectionMode = if (document.pageConfig == "Tất cả") "Tất cả" else "Chọn khoảng"

    // Hiệu ứng xoay mượt mà cho mũi tên chính của thẻ
    val arrowRotationDegree by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ================= HÀNG HIỂN THỊ CHÍNH (GIỮ NGUYÊN STYLE ĐẸP CỦA BẠN) =================
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (document.isPdf) isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tổng số: ${document.totalPages} trang",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (document.isPdf) {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Cấu hình trang",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(arrowRotationDegree)
                        )
                    }
                }
            }

            // ================= KHOANG NỘI DUNG SỔ XUỐNG KHI BẤM MŨI TÊN =================
            androidx.compose.animation.AnimatedVisibility(
                visible = document.isPdf && isExpanded,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // --- HÀNG CẤU HÌNH: CHỌN DROP DOWN MENU ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cấu hình:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Hộp chọn Dropdown chế độ (Material 3 ExposedDropdownMenuBox)
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                        ) {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier
                                    .menuAnchor() // Liên kết vị trí thả menu
                                    .width(160.dp)
                                    .height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectionMode,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = if (isDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Thực đơn đổ xuống hiển thị đúng 2 tùy chọn như ảnh mẫu của bạn
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tất cả", fontSize = 14.sp) },
                                    onClick = {
                                        onPageConfigChange("Tất cả")
                                        isDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Chọn khoảng", fontSize = 14.sp) },
                                    onClick = {
                                        // Gợi ý khoảng mặc định ban đầu khi chuyển sang chọn khoảng
                                        onPageConfigChange("1-${document.totalPages}")
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // --- KHOANG CON CẤU HÌNH Ô NHẬP KHOẢNG (CHỈ HIỆN KHI CHỌN "CHỌN KHOẢNG") ---
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectionMode == "Chọn khoảng",
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Chọn khoảng:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val currentRange = document.pageConfig.split("-")
                                    var fromPage by remember(document.pageConfig) { mutableStateOf(currentRange.getOrNull(0) ?: "1") }
                                    var toPage by remember(document.pageConfig) { mutableStateOf(currentRange.getOrNull(1) ?: document.totalPages.toString()) }

                                    // Ô nhập trang BẮT ĐẦU
                                    OutlinedTextField(
                                        value = fromPage,
                                        onValueChange = { input ->
                                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                                fromPage = input
                                                val fromInt = input.toIntOrNull() ?: 1
                                                val toInt = toPage.toIntOrNull() ?: document.totalPages
                                                if (fromInt in 1..document.totalPages && fromInt <= toInt) {
                                                    onPageConfigChange("$fromPage-$toInt")
                                                }
                                            }
                                        },
                                        placeholder = { Text("Từ", fontSize = 11.sp, textAlign = TextAlign.Center) },
                                        modifier = Modifier.width(55.dp).height(46.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
                                    )

                                    Text(text = "đến", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)

                                    // Ô nhập trang KẾT THÚC
                                    OutlinedTextField(
                                        value = toPage,
                                        onValueChange = { input ->
                                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                                toPage = input
                                                val fromInt = fromPage.toIntOrNull() ?: 1
                                                val toInt = input.toIntOrNull() ?: document.totalPages
                                                if (toInt in 1..document.totalPages && fromInt <= toInt) {
                                                    onPageConfigChange("$fromInt-$toPage")
                                                }
                                            }
                                        },
                                        placeholder = { Text("Đến", fontSize = 11.sp, textAlign = TextAlign.Center) },
                                        modifier = Modifier
                                            .width(60.dp) // Tăng nhẹ độ rộng để gõ số có 2-3 chữ số không bị che
                                            .height(46.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
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
        Text(text = "Hệ thống đang chạy tiến trình nén phân nhánh ngầm...", fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun ConfigureMatrixLayout(
    config: AiMatrixConfig,
    onTopicChange: (String) -> Unit,
    onPresetSelect: (Boolean) -> Unit,
    onSliderChange: (Float, Float, Float) -> Unit,
    onGenerateClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Chủ đề nhận diện được:", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = config.suggestedTopic,
            onValueChange = onTopicChange,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Cấu hình số lượng câu hỏi:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = config.easyCount == 3f && config.midCount == 1f && config.hardCount == 0f,
                onClick = { onPresetSelect(false) },
                label = { Text("Chế độ 'Dễ thở' (3-1-0)") }
            )
            FilterChip(
                selected = config.easyCount == 1f && config.midCount == 3f && config.hardCount == 2f,
                onClick = { onPresetSelect(true) },
                label = { Text("Chế độ 'Try-hard' (1-3-2)") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DifficultySliderItem(label = "Dễ (ez)", value = config.easyCount) { onSliderChange(it, config.midCount, config.hardCount) }
        DifficultySliderItem(label = "Trung bình (mid)", value = config.midCount) { onSliderChange(config.easyCount, it, config.hardCount) }
        DifficultySliderItem(label = "Khó (hard)", value = config.hardCount) { onSliderChange(config.easyCount, config.midCount, it) }

        Spacer(modifier = Modifier.height(16.dp))

        val totalQuestions = (config.easyCount + config.midCount + config.hardCount).toInt()
        Button(
            onClick = onGenerateClick,
            enabled = totalQuestions > 0,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Bắt đầu tạo $totalQuestions câu hỏi bằng AI", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun DifficultySliderItem(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, fontSize = 13.sp)
            Text(text = "${value.toInt()} câu", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
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