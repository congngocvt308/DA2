package com.cong.smartalarm.feature.topic

import androidx.compose.ui.graphics.Color

import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.theme.Calm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.feature.quiz.QuestionData
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun QuestionCard(
    question: QuestionData,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    CalmCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Calm.Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Calm.Border
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SmartMathText(
                    text = question.questionText,
                    isTitle = true,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                val correctAnswerText = question.answers.find { it.isCorrect }?.text
                    ?: "Chưa có đáp án đúng"
                SmartMathText(
                    text = correctAnswerText,
                    isTitle = false,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun SmartMathText(
    text: String,
    isTitle: Boolean,
    maxLines: Int,
    modifier: Modifier = Modifier
) {
    // Bộ lọc kiểm tra xem chuỗi có thực sự dính ký tự toán học bọc dấu $ không
    val containsMath = remember(text) {
        text.contains("$")
    }

    if (containsMath) {
        // Nếu chứa công thức toán -> Kích hoạt MathText chạy KaTeX local siêu tốc
        MathText(
            latexText = text,
            modifier = modifier,
            fontSize = if (isTitle) 16 else 14,
            textColor = if (isTitle) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiary,
            isBold = isTitle,
            maxLines = maxLines
        )
    } else {
        // Nếu thuần văn bản thường -> Dùng Text Native nhẹ hửng, cắt đuôi dấu 3 chấm chuẩn UX
        Text(
            text = text,
            color = if (isTitle) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.tertiary,
            fontWeight = if (isTitle) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTitle) 16.sp else 14.sp,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
    }
}