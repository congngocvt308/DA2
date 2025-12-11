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

@Composable
fun QuizScreen(
    viewModel: QuizViewModel = viewModel(),
    onBack: () -> Unit,
    onQuizCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = uiState.questionPool.getOrNull(uiState.poolIndex)

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onQuizCompleted()
        }
    }

    if (currentQuestion == null || uiState.questionPool.isEmpty()) {
        return Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
        }
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
            color = if (timerProgress < 0.3f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.tertiary,
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

    val targetColor = when {
        uiState.isAnswered && answer.isCorrect -> MaterialTheme.colorScheme.secondary
        uiState.isAnswered && isSelected && !answer.isCorrect -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val backgroundColor by animateColorAsState(targetValue = targetColor, label = "OptionColor")

    Button(
        onClick = onSelect,
        enabled = !uiState.isAnswered,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = backgroundColor
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().height(60.dp)
    ) {
        Text(answer.text, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}