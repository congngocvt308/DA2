package com.cong.smartalarm.ui.theme.alarm
import com.cong.smartalarm.ui.theme.components.LiquidGlass
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import com.cong.smartalarm.ui.theme.theme.Calm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.data.AnswerData
import com.cong.smartalarm.data.QuizUiStateData

@Composable
fun QuizScreen(
    alarmId: Int = -1,
    onBack: () -> Unit,
    onQuizCompleted: () -> Unit,
    viewModel: QuizViewModel = viewModel()
) {
    // Set alarmId khi khởi tạo
    LaunchedEffect(alarmId) {
        viewModel.setAlarmId(alarmId)
    }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onQuizCompleted()
        }
    }

    if (uiState.isLoading) {
        return Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Đang chọn câu hỏi phù hợp...", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }

    val currentQuestion = uiState.questionPool.getOrNull(uiState.poolIndex)
    if (currentQuestion == null) {
        return Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            if (uiState.isTimeOut) {
                Text(
                    "HẾT GIỜ!",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = currentQuestion.questionText,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                currentQuestion.answers.forEach { answer ->
                    QuizOptionButton(
                        answer = answer,
                        uiState = uiState,
                        onSelect = {
                            viewModel.onOptionSelected(answer.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun QuizTopBar(
    timerProgress: Float,
    currentIndex: Int,
    total: Int,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { timerProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = if (timerProgress < 0.3f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = Calm.LavenderMist,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = MaterialTheme.colorScheme.onBackground) }
            Text(text = "$currentIndex/$total", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.VolumeUp, "Âm thanh", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
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
    val isCorrectAndAnswered = uiState.isAnswered && answer.isCorrect
    val isWrongAndSelected = uiState.isAnswered && isSelected && !answer.isCorrect

    // 🎯 1. ĐỊNH NGHĨA BẢNG MÀU CHUẨN (Calm)
    val colorSuccess = Calm.WellnessText // Xanh lá đậm: chữ trắng đủ tương phản
    val colorError = Calm.Destructive    // Đỏ
    val colorDefault = Calm.Surface.copy(alpha = 0f) // Giữ bề mặt trắng nổi (trắng trong suốt để chuyển màu không bị xám)
    val colorSelected = MaterialTheme.colorScheme.primaryContainer

    val targetColor = when {
        isCorrectAndAnswered -> colorSuccess
        isWrongAndSelected -> colorError
        isSelected -> colorSelected
        else -> colorDefault
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "OptionColor"
    )

    // 🎯 2. PHẢN HỒI KHI CHẠM (SCALE) — nhẹ, không nảy
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "Scale"
    )

    // Nút đáp án nổi mềm: màu trạng thái tô lên bề mặt trắng
    val contentColor = if (uiState.isAnswered && (answer.isCorrect || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface
    LiquidGlass(
        onClick = onSelect,
        enabled = !uiState.isAnswered && !uiState.isTimeOut,
        cornerRadius = 16.dp,
        tint = SolidColor(backgroundColor),
        elevation = if (isSelected || uiState.isAnswered) 2.dp else 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // Không chỉ dựa vào màu: thêm biểu tượng đúng / sai
                if (isCorrectAndAnswered) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Đáp án đúng", modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                } else if (isWrongAndSelected) {
                    Icon(Icons.Default.Cancel, contentDescription = "Đáp án sai", modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = answer.text,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected || isCorrectAndAnswered) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
