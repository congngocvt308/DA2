package com.cong.smartalarm.ui.theme.facedetection
import com.cong.smartalarm.ui.theme.components.LocalGlassBackdrop
import androidx.compose.runtime.CompositionLocalProvider

import com.cong.smartalarm.ui.theme.components.GlassCard
import com.cong.smartalarm.ui.theme.components.GlassAlertDialog
import com.cong.smartalarm.ui.theme.theme.Calm

import android.os.Build
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil.compose.AsyncImage
import com.cong.smartalarm.data.PictureEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImageDetailDialog(
    picture: PictureEntity,
    onDismiss: () -> Unit,
    onDeletePhoto: (PictureEntity) -> Unit,
    onUpdateDescription: (PictureEntity, String) -> Unit // 🚀 Callback để lưu mô tả mới
) {
    // Màn có camera/ảnh phía sau: kính không khúc xạ nền aurora (sẽ lệch), dùng kính "khói".
    CompositionLocalProvider(LocalGlassBackdrop provides null) {
        ImageDetailDialogContent(picture, onDismiss, onDeletePhoto, onUpdateDescription)
    }
}

@Composable
private fun ImageDetailDialogContent(
    picture: PictureEntity,
    onDismiss: () -> Unit,
    onDeletePhoto: (PictureEntity) -> Unit,
    onUpdateDescription: (PictureEntity, String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var isEditingText by remember { mutableStateOf(false) }
    var descriptionText by remember(picture.description) {
        mutableStateOf(picture.description?.ifBlank { "Nụ cười kỷ niệm" } ?: "Nụ cười kỷ niệm")
    }

    val formattedSaveTime = remember(picture.createdAt) {
        val date = Date(picture.createdAt)
        SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.let { window ->
            window.setDimAmount(0.3f)
            window.setBackgroundDrawableResource(android.R.color.transparent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = 60
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PhotoScrim),
            contentAlignment = Alignment.Center
        ) {
            // Nút Đóng
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
                    .background(Calm.Surface, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Calm.Foreground
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. HIỂN THỊ ẢNH CHÍNH
                GlassCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Calm.LavenderMist),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    AsyncImage(
                        model = picture.imageUri,
                        contentDescription = picture.description ?: "Chi tiết ảnh",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. KHUNG THÔNG TIN KÝ ỨC (Bấm vào để SỬA TEXT)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Calm.Surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isEditingText = true } // 🚀 Bấm vào khung này để bật chế độ sửa text
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isEditingText) {
                            BasicTextField(
                                value = descriptionText,
                                onValueChange = { descriptionText = it },
                                textStyle = TextStyle(
                                    color = Calm.Foreground,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(Calm.Primary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        isEditingText = false
                                        if (descriptionText.isNotBlank() && descriptionText != picture.description) {
                                            onUpdateDescription(picture, descriptionText)
                                            Toast.makeText(context, "Đã cập nhật mô tả!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = descriptionText,
                                    color = Calm.Foreground,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Sửa",
                                    tint = Calm.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Thời gian lưu: $formattedSaveTime",
                            color = Calm.MutedForeground,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3. THANH CÔNG CỤ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        icon = Icons.Default.Share,
                        label = "Chia sẻ",
                        backgroundColor = Calm.Surface,
                        tint = Calm.Primary
                    ) {
                        Toast.makeText(context, "Tính năng chia sẻ đang phát triển!", Toast.LENGTH_SHORT).show()
                    }

                    ActionButton(
                        icon = Icons.Default.Download,
                        label = "Tải về",
                        backgroundColor = Calm.Surface,
                        tint = Calm.WellnessText
                    ) {
                        Toast.makeText(context, "Tính năng tải về đang phát triển!", Toast.LENGTH_SHORT).show()
                    }

                    ActionButton(
                        icon = Icons.Default.Delete,
                        label = "Xóa",
                        backgroundColor = Calm.Surface,
                        tint = Calm.Destructive
                    ) {
                        showDeleteConfirm = true
                    }
                }
            }
        }
    }

    // Dialog xác nhận xóa
    if (showDeleteConfirm) {
        GlassAlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xác nhận xóa", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa nụ cười này khỏi Bảo Tàng?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeletePhoto(picture)
                        Toast.makeText(context, "Đã xóa ảnh thành công!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                ) {
                    Text("Xóa", color = Calm.Destructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Nhãn nằm trực tiếp trên lớp phủ tối -> giữ chữ trắng
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Lớp phủ tối phía sau ảnh (thay cho đen thuần). */
private val PhotoScrim = Color(0xFF09090B).copy(alpha = 0.88f)
