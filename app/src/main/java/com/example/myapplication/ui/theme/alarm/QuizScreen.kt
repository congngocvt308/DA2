package com.cong.smartalarm.ui.theme.alarm

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (timerProgress < 0.3f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onBackground) }
            Text(text = "$currentIndex/$total", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = MaterialTheme.colorScheme.onBackground) }
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

    // 🎯 1. ĐỊNH NGHĨA BẢNG MÀU CHUẨN
    val colorSuccess = Color(0xFF4CAF50) // Xanh lá
    val colorError = Color(0xFFF44336)   // Đỏ
    val colorDefault = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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

    // 🎯 2. HIỆU ỨNG NHẤP NHÁY CHO CÂU ĐÚNG
    val infiniteTransition = rememberInfiniteTransition(label = "Blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCorrectAndAnswered) 0.6f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlinkAlpha"
    )

    // 🎯 3. HIỆU ỨNG PHẢN HỒI KHI CHẠM (SCALE)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "Scale"
    )

    Surface(
        onClick = onSelect,
        enabled = !uiState.isAnswered && !uiState.isTimeOut,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor.copy(alpha = if (isCorrectAndAnswered) blinkAlpha else 1f),
        contentColor = if (uiState.isAnswered && (answer.isCorrect || isSelected)) Color.White else MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (isSelected || uiState.isAnswered) 0.dp else 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = answer.text,
                fontSize = 18.sp,
                fontWeight = if (isSelected || isCorrectAndAnswered) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
