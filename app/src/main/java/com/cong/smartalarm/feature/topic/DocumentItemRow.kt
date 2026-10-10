package com.cong.smartalarm.feature.topic

import androidx.compose.ui.graphics.graphicsLayer
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.feature.topic.model.SelectedDocument

/** Thẻ tài liệu trong hàng chờ, PDF có khoang chọn khoảng trang. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentItemRow(
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

    CalmCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Calm.Surface
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
                    tint = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error,
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
                                .graphicsLayer { rotationZ = arrowRotationDegree }
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
                        .background(Calm.Background)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
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
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Calm.Surface,
                                    contentColor = Calm.Foreground
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
                                color = MaterialTheme.colorScheme.outlineVariant,
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
                                        modifier = Modifier.width(55.dp).height(48.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
                                    )

                                    Text(text = "đến", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

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
                                            .height(48.dp),
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
