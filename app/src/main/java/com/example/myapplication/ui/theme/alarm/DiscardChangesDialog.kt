package com.cong.smartalarm.ui.theme.alarm

import com.cong.smartalarm.ui.theme.components.GlassCard
import com.cong.smartalarm.ui.theme.components.GlassButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DiscardChangesDialog(
    onDismissRequest: () -> Unit,
    onConfirmDiscard: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        GlassCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Hủy thay đổi?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Bạn có chắc muốn thoát không? Mọi thay đổi chưa lưu sẽ bị mất.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GlassButton(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Text("Quay lại", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    GlassButton(
                        onClick = onConfirmDiscard,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) {
                        Text("Thoát", color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}