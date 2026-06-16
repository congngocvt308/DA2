package com.example.myapplication.ui.theme.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.QuestionData

@Composable
fun QuestionDetailDialog(
    question: QuestionData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 650.dp), // Giới hạn chiều cao vừa vặn cho Dialog xem chi tiết
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Thanh Tiêu đề chỉ giữ lại nút Đóng (X)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chi tiết câu hỏi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { onDismiss() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. VÙNG HIỂN THỊ NỘI DUNG CÂU HỎI CHÍNH (Đọc trọn vẹn Toán học)
                    Text(
                        text = "Đề bài:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        SmartMathText(
                            text = question.questionText,
                            isTitle = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Các phương án trả lời:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. DANH SÁCH CÁC ĐÁP ÁN CHỈ ĐỌC (READ-ONLY)
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(question.answers) { answer ->
                            AnswerDetailRow(
                                text = answer.text,
                                isCorrect = answer.isCorrect
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nút đóng hộp thoại ở dưới cùng
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Đóng lại", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerDetailRow(
    text: String,
    isCorrect: Boolean
) {
    // Nếu là đáp án đúng -> Bo viền màu xanh lá/Secondary, nếu sai -> Bo viền xám nhạt
    val borderColor = if (isCorrect) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = if (isCorrect) MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f) else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Thay RadioButton bấm được bằng RadioButton khóa trạng thái chỉ xem
        RadioButton(
            selected = isCorrect,
            onClick = null, // Vô hiệu hóa tương tác click đổi trạng thái
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.secondary,
                unselectedColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Hiển thị nội dung đáp án qua trạm gác Toán học
        Box(modifier = Modifier.weight(1f)) {
            SmartMathText(
                text = text,
                isTitle = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * TRẠM GÁC SMART_MATH_TEXT:
 * Cho phép hiển thị tối đa 5 dòng (hoặc tự co giãn xuống dòng) để xem trọn vẹn công thức phức tạp.
 */
@Composable
private fun SmartMathText(
    text: String,
    isTitle: Boolean,
    modifier: Modifier = Modifier
) {
    val containsMath = remember(text) {
        text.contains("$") || text.contains("$$")
    }

    if (containsMath) {
        MathText(
            latexText = text,
            modifier = modifier.wrapContentHeight()
        )
    } else {
        Text(
            text = text,
            color = if (isTitle) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTitle) 16.sp else 14.sp,
            maxLines = 5, // Tăng maxLines lên để người dùng xem được trọn vẹn câu hỏi dài
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }
}