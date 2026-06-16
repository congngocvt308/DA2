package com.example.myapplication.ui.theme.topic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.QuestionData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathTestScreen() {
    var selectedQuestion by remember { mutableStateOf<QuestionData?>(null) }
    // 1. Khởi tạo danh sách câu hỏi trắc nghiệm Toán học chứa LaTeX giả lập (Mock Data)
    val mockQuestions = listOf(
        QuestionData(
            id = 1,
            questionText = "Tìm nghiệm của phương trình bậc hai sau: \$x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}\$",
            answers = listOf(
                AnswerData(id = 1,text = "Nghiệm duy nhất khi \$\\Delta = 0\$", isCorrect = true),
                AnswerData(id = 2,text = "Vô nghiệm khi \$\\Delta < 0\$", isCorrect = false)
            )
        ),
        QuestionData(
            id = 2,
            questionText = "Tính giá trị của biểu thức tích phân cao cấp: \$\\int_{0}^{\\pi} \\sin(x) dx\$",
            answers = listOf(
                AnswerData(id = 3,text = "Kết quả bằng: \$2\$", isCorrect = true),
                AnswerData(id = 4,text = "Kết quả bằng: \$0\$", isCorrect = false)
            )
        ),
        QuestionData(
            id = 3,
            questionText = "Cho ma trận vuông cấp hai \$A = \\begin{pmatrix} a & b \\\\ c & d \\end{pmatrix}\$. Tìm định thức \$\\det(A)\$",
            answers = listOf(
                AnswerData(id = 5,text = "Chưa có đáp án đúng", isCorrect = false)
            )
        ),
        QuestionData(
            id = 4,
            questionText = "Tính đạo hàm bậc hai của hàm số mũ phức tạp sau: \$\\frac{d}{dx} e^{x^2}\$",
            answers = listOf(
                AnswerData(id = 6,text = "Kết quả: \$2x e^{x^2}\$", isCorrect = true),
                AnswerData(id = 7,text = "Kết quả: \$e^{x^2}\$", isCorrect = false)
            )
        )
    )

    Scaffold(
    ) { innerPadding ->
        // 2. Render danh sách các thẻ câu hỏi trắc nghiệm bằng LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockQuestions) { question ->
                QuestionCard(
                    question = question,
                    onDelete = {
                        // Callback xử lý xóa câu hỏi (tạm thời in log để test)
                        println("Bấm xóa câu hỏi: ${question.questionText}")
                    },
                    onClick = {
                        selectedQuestion = question
                    }
                )
            }
        }
    }
    selectedQuestion?.let { question ->
        QuestionDetailDialog(
            question = question,
            onDismiss = { selectedQuestion = null }
        )
    }
}