package com.example.myapplication.ui.theme.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.ui.theme.topic.QuickTopicContent

// Màu sắc (Giả định)
private val DialogBg = Color(0xFF1C1C1E)
private val RedWarning = Color(0xFFE53935)
private val TextWhite = Color.White
private val TextGray = Color.Gray

@Composable
fun DiscardChangesDialog(
    onDismissRequest: () -> Unit, // Bấm ra ngoài hoặc bấm "Tiếp tục sửa"
    onConfirmDiscard: () -> Unit  // Bấm "Thoát"
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tiêu đề
                Text(
                    text = "Hủy thay đổi?",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nội dung
                Text(
                    text = "Bạn có chắc muốn thoát không? Mọi thay đổi chưa lưu sẽ bị mất.",
                    color = TextGray,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Hàng nút bấm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Nút "Tiếp tục sửa" (Giữ lại màn hình)
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Quay lại", color = TextWhite)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Nút "Thoát" (Hủy và thoát)
                    Button(
                        onClick = onConfirmDiscard,
                        colors = ButtonDefaults.buttonColors(containerColor = RedWarning),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Thoát", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}