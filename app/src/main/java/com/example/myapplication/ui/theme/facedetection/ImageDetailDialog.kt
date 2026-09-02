package com.example.myapplication.ui.theme.facedetection

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
import com.example.myapplication.data.PictureEntity
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
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            // Nút Đóng
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. HIỂN THỊ ẢNH CHÍNH
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
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
                    color = Color.Black.copy(alpha = 0.65f),
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
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(Color(0xFFFF4081)),
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
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Sửa",
                                    tint = Color(0xFFFF4081),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Thời gian lưu: $formattedSaveTime",
                            color = Color(0xFFE0E0E0),
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
                        backgroundColor = Color(0xFF333333),
                        tint = Color(0xFF80D8FF)
                    ) {
                        Toast.makeText(context, "Tính năng chia sẻ đang phát triển!", Toast.LENGTH_SHORT).show()
                    }

                    ActionButton(
                        icon = Icons.Default.Download,
                        label = "Tải về",
                        backgroundColor = Color(0xFF333333),
                        tint = Color(0xFFB2FF59)
                    ) {
                        Toast.makeText(context, "Tính năng tải về đang phát triển!", Toast.LENGTH_SHORT).show()
                    }

                    ActionButton(
                        icon = Icons.Default.Delete,
                        label = "Xóa",
                        backgroundColor = Color(0xFF333333),
                        tint = Color(0xFFFF5252)
                    ) {
                        showDeleteConfirm = true
                    }
                }
            }
        }
    }

    // Dialog xác nhận xóa
    if (showDeleteConfirm) {
        AlertDialog(
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
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
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
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}