package com.cong.smartalarm.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cong.smartalarm.core.designsystem.component.CalmAlertDialog
import com.cong.smartalarm.core.designsystem.component.PermissionItem

/** Dialog yêu cầu quyền hệ thống (báo thức chính xác, máy ảnh, thông báo). */
@Composable
internal fun PermissionDialog(
    hasAlarmPermission: Boolean,
    hasCameraPermission: Boolean,
    hasNotificationPermission: Boolean,
    onDismiss: () -> Unit,
    onRequestPermissions: (Array<String>) -> Unit
) {
    val context = LocalContext.current
    CalmAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yêu cầu hệ thống 🚨", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Vui lòng cấp các quyền dưới đây để app hoạt động chuẩn xác:")
                Spacer(modifier = Modifier.height(8.dp))

                PermissionItem(
                    title = "Báo thức & Nhắc nhở",
                    description = "Đảm bảo reo đúng từng giây",
                    icon = Icons.Default.Alarm,
                    isGranted = hasAlarmPermission,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                // 1. Quyền Máy ảnh
                // 1. Quyền Máy ảnh
                PermissionItem(
                    title = "Máy ảnh",
                    description = "Dùng để quét mã QR tắt báo thức",
                    icon = Icons.Default.CameraAlt,
                    isGranted = hasCameraPermission,
                    onClick = {
                        if (!hasCameraPermission) {
                            // Nếu hệ thống không cho hiện popup nữa, ta mở Cài đặt
                            // Bạn có thể dùng ActivityCompat.shouldShowRequestPermissionRationale để check kỹ hơn
                            onRequestPermissions(arrayOf(android.Manifest.permission.CAMERA))

                            openAppSettings(context)
                        }
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionItem(
                        title = "Thông báo",
                        description = "Hiển thị báo thức và lời nhắc",
                        icon = Icons.Default.Notifications,
                        isGranted = hasNotificationPermission,
                        onClick = {
                            if (!hasNotificationPermission) {
                                onRequestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS))
                                openAppSettings(context)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

// Mở trang cài đặt của app để người dùng tự cấp quyền
fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}