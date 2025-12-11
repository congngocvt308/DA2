package com.example.myapplication.ui.theme.topic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AnswerData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.QuestionData
import com.example.myapplication.data.QuestionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TopicDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val topicDao = AppDatabase.getDatabase(application).appDao()
    private val topicId: Int = savedStateHandle.get<Int>("topicId") ?: -1

    private val _dbQuestions = MutableStateFlow<List<QuestionData>>(emptyList())
    val searchQuery = MutableStateFlow("")
    private val _topicName = MutableStateFlow("")
    val topicName = _topicName.asStateFlow()
    val filteredQuestions: StateFlow<List<QuestionData>> = combine(
        _dbQuestions,
        searchQuery
    ) { questions, query ->
        if (query.isBlank()) questions
        else questions.filter { it.questionText.contains(query, ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val questions = _dbQuestions.asStateFlow()

    init {
        if (topicId != -1) {
            loadQuestions()
            loadTopicInfo()
        }
    }

    private fun loadTopicInfo() {
        viewModelScope.launch {
            val name = topicDao.getTopicNameById(topicId)
            if (name != null) {
                _topicName.value = name
            }
        }
    }

    fun renameTopic(newName: String) {
        viewModelScope.launch {
            topicDao.updateTopicName(topicId, newName)
            _topicName.value = newName
        }
    }

    fun deleteCurrentTopic(onDeleted: () -> Unit) {
        viewModelScope.launch {
            topicDao.deleteTopicById(topicId)
            onDeleted()
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            topicDao.getQuestionsByTopic(topicId).collect { entities ->
                val mappedList = entities.map { entity ->
                    val correctAns = AnswerData(0, entity.correctAnswer, true)
                    val wrongAns = entity.options.mapIndexed { index, text ->
                        AnswerData(index + 1, text, false)
                    }
                    val allAnswers = (listOf(correctAns) + wrongAns).shuffled()
                    QuestionData(id = entity.questionId, questionText = entity.prompt, answers = allAnswers)
                }
                _dbQuestions.value = mappedList
            }
        }
    }

    fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            val questionEntity = topicDao.getQuestionById(questionId)
            if (questionEntity != null) {
                topicDao.deleteQuestion(questionEntity)
            }
        }
    }

    private var editingQuestionId: Int = -1

    private val _draftQuestionText = MutableStateFlow("")
    val draftQuestionText = _draftQuestionText.asStateFlow()

    private val _draftAnswers = MutableStateFlow(
        listOf(AnswerData(0, "", false), AnswerData(1, "", true))
    )

    val draftAnswers = _draftAnswers.asStateFlow()

    fun onQuestionTextChange(text: String) { _draftQuestionText.value = text }

    fun onAnswerTextChange(index: Int, text: String) {
        _draftAnswers.update { list ->
            val newList = list.toMutableList()
            newList[index] = newList[index].copy(text = text)
            newList
        }
    }

    fun onSelectCorrectAnswer(selectedIndex: Int) {
        _draftAnswers.update { list ->
            list.mapIndexed { index, answer ->
                answer.copy(isCorrect = (index == selectedIndex))
            }
        }
    }

    fun onAddAnswerLine() {
        _draftAnswers.update { list ->
            val newId = (list.maxOfOrNull { it.id } ?: 0) + 1
            list + AnswerData(newId, "", false)
        }
    }

    fun onRemoveAnswerLine(index: Int) {
        _draftAnswers.update { list ->
            if (list.size > 1) {
                val newList = list.toMutableList()
                newList.removeAt(index)
                newList
            } else {
                list
            }
        }
    }

    fun resetDraft() {
        editingQuestionId = -1
        _draftQuestionText.value = ""
        _draftAnswers.value = listOf(
            AnswerData(0, "", false),
            AnswerData(1, "", false)
        )
    }

    fun startEditing(question: QuestionData) {
        editingQuestionId = question.id
        _draftQuestionText.value = question.questionText
        _draftAnswers.value = question.answers
    }

    fun saveQuestion() {
        viewModelScope.launch {
            if (topicId == -1) return@launch
            val currentAnswers = _draftAnswers.value
            val questionText = _draftQuestionText.value
            val correctObj = currentAnswers.find { it.isCorrect }
            val correctText = correctObj?.text ?: ""
            val wrongOptions = currentAnswers.filter { !it.isCorrect }.map { it.text }
            val questionEntity = QuestionEntity(
                questionId = if (editingQuestionId == -1) 0 else editingQuestionId,
                ownerTopicId = topicId,
                prompt = questionText,
                options = wrongOptions,
                correctAnswer = correctText
            )
            if (editingQuestionId == -1) {
                topicDao.insertQuestion(questionEntity)
            } else {
                topicDao.updateQuestion(questionEntity)
            }
            resetDraft()
            loadQuestions()
        }
    }
}