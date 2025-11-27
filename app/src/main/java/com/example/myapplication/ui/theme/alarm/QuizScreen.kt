package com.example.myapplication.ui.theme.alarm

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.QuizUiStateData

// Màu sắc theo thiết kế
val QuizBackground = Color.Black
val OptionNormal = Color(0xFF333333) // Xám đậm
val OptionError = Color(0xFFEE4540)  // Đỏ (Sai)
val OptionSuccess = Color(0xFF4CAF50) // Xanh (Đúng)

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = viewModel(),
    onBack: () -> Unit,
    onQuizCompleted: () -> Unit // Gọi khi làm xong hết nhiệm vụ (Tắt báo thức)
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = uiState.questionPool.getOrNull(uiState.poolIndex)

    // Logic tự động chuyển câu hoặc hoàn thành khi trả lời đúng
    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            // TODO: Xử lý dừng nhạc chuông
            onQuizCompleted()
        }
    }

    // Xử lý trạng thái tải/rỗng
    if (currentQuestion == null || uiState.questionPool.isEmpty()) {
        return Box(modifier = Modifier.fillMaxSize().background(QuizBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    Scaffold(
        containerColor = QuizBackground,
        topBar = {
            QuizTopBar(
                timerProgress = uiState.timerProgress,
                currentIndex = uiState.correctlyAnsweredCount,
                total = uiState.targetCorrectAnswers,
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // 1. Nội dung câu hỏi
            // Spacer để đẩy câu hỏi lên giữa một chút
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = currentQuestion.questionText,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // 2. Danh sách đáp án
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Hiển thị tất cả các đáp án (options + correct answer)
                currentQuestion.answers.forEach { answer ->
                    QuizOptionButton(
                        answer = answer,
                        uiState = uiState, // Truyền trạng thái để tính toán màu
                        onSelect = {
                            // 🚨 GỌI VIEWMODEL KHI CLICK 🚨
                            viewModel.onOptionSelected(answer.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- Component: Thanh tiêu đề (Top Bar) ---
@Composable
fun QuizTopBar(
    timerProgress: Float,
    currentIndex: Int,
    total: Int,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 50.dp)) {
        // Thanh tiến trình
        LinearProgressIndicator(
            progress = { timerProgress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (timerProgress < 0.3f) Color(0xFFEE4540) else Color(0xFF4CAF50),
            trackColor = Color.DarkGray,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
            // Hiển thị tiến trình thành công
            Text(text = "$currentIndex/$total", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color.White) }
        }
    }
}

@Composable
fun QuizOptionButton(
    answer: AnswerData,
    uiState: QuizUiStateData,
    onSelect: () -> Unit
) {
    val isSelected = (answer.id == uiState.selectedAnswerId)

    // Logic màu sắc
    val targetColor = when {
        // 1. Nếu đã trả lời và là đáp án ĐÚNG (luôn hiện Xanh)
        uiState.isAnswered && answer.isCorrect -> OptionSuccess

        // 2. Nếu đã trả lời, được người dùng chọn, và SAI -> Đỏ
        uiState.isAnswered && isSelected && !answer.isCorrect -> OptionError

        // 3. Nếu đang được chọn (trước khi phản hồi)
        isSelected -> Color(0xFF555555) // Xám đậm hơn để báo hiệu đang chọn

        // 4. Mặc định
        else -> OptionNormal
    }

    val backgroundColor by animateColorAsState(targetValue = targetColor, label = "OptionColor")

    Button(
        onClick = onSelect,
        enabled = !uiState.isAnswered, // Vô hiệu hóa khi đã có phản hồi màu
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().height(60.dp)
    ) {
        Text(answer.text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}