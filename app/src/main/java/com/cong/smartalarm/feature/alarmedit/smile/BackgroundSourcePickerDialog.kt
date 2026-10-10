package com.cong.smartalarm.feature.alarmedit.smile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm

/** Chọn nguồn hình nền: ảnh mặc định hoặc ảnh từ bộ nhớ. */
@Composable
fun BackgroundSourcePickerDialog(
    onDismiss: () -> Unit,
    onDefaultSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CalmCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Chọn hình nền", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    onClick = onDefaultSelected,
                    shape = RoundedCornerShape(16.dp),
                    color = Calm.NeutralMist,
                    contentColor = Calm.Foreground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Calm.Surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Landscape, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Ảnh mặc định", fontWeight = FontWeight.Bold)
                            Text("Cảnh hoàng hôn", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    onClick = onGallerySelected,
                    shape = RoundedCornerShape(16.dp),
                    color = Calm.NeutralMist,
                    contentColor = Calm.Foreground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Calm.Surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, tint = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Từ bộ nhớ", fontWeight = FontWeight.Bold)
                            Text("Chọn ảnh của bạn", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        }
    }
}
