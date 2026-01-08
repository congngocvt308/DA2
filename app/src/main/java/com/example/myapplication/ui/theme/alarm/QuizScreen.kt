package com.example.myapplication.ui.theme.alarm

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
    Column(modifier = Modifier.fillMaxWidth().padding(top = 50.dp)) {
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
    val infiniteTransition = rememberInfiniteTransition(label = "Blink")
    val isCorrectAndAnswered = uiState.isAnswered && answer.isCorrect

    val successColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error
    val selectedColor = MaterialTheme.colorScheme.surfaceVariant
    val defaultColor = MaterialTheme.colorScheme.surface

    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCorrectAndAnswered) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlinkAlpha"
    )
    val targetColor = when {
        uiState.isAnswered && answer.isCorrect -> successColor
        uiState.isAnswered && isSelected && !answer.isCorrect -> errorColor
        isSelected -> selectedColor
        else -> defaultColor
    }

    val usesPrimaryContentColor = uiState.isAnswered && (answer.isCorrect || (isSelected && !answer.isCorrect))
    val contentColor = if (usesPrimaryContentColor) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400),
        label = "OptionColor"
    )

    Button(
        onClick = onSelect,
        enabled = !uiState.isAnswered && !uiState.isTimeOut,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor.copy(alpha = if (isCorrectAndAnswered) blinkAlpha else 1f),
            disabledContainerColor = backgroundColor.copy(alpha = if (isCorrectAndAnswered) blinkAlpha else 1f),
            contentColor = contentColor,
            disabledContentColor = contentColor
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Text(answer.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}