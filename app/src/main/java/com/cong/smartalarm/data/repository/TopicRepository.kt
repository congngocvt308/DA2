package com.cong.smartalarm.data.repository

import com.cong.smartalarm.core.database.dao.AppDao
import com.cong.smartalarm.core.database.entity.QuestionEntity
import com.cong.smartalarm.core.database.entity.TopicEntity
import com.cong.smartalarm.core.database.entity.TopicWithQuestions
import com.cong.smartalarm.core.database.model.TopicWithCountResult
import com.cong.smartalarm.core.network.AiApiService
import com.cong.smartalarm.core.network.GenerateQuestionsRequest
import com.cong.smartalarm.core.network.OcrResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

/** Chủ đề, câu hỏi và máy chủ AI phân tích tài liệu / sinh câu hỏi. */
class TopicRepository(
    private val dao: AppDao,
    private val aiApi: AiApiService
) {
    // --- Chủ đề ---
    fun getAllTopicsWithCount(): Flow<List<TopicWithCountResult>> = dao.getAllTopicsWithCount()
    fun getTopicsWithQuestions(): Flow<List<TopicWithQuestions>> = dao.getTopicsWithQuestions()
    suspend fun insertTopic(topic: TopicEntity): Long = dao.insertTopic(topic)
    suspend fun getTopicNameById(id: Int): String? = dao.getTopicNameById(id)
    suspend fun updateTopicName(topicId: Int, newName: String) = dao.updateTopicName(topicId, newName)
    suspend fun deleteTopicById(id: Int) = dao.deleteTopicById(id)

    // --- Câu hỏi ---
    fun getQuestionsByTopic(topicId: Int): Flow<List<QuestionEntity>> = dao.getQuestionsByTopic(topicId)
    suspend fun getQuestionById(id: Int): QuestionEntity? = dao.getQuestionById(id)
    suspend fun insertQuestion(question: QuestionEntity): Long = dao.insertQuestion(question)
    suspend fun updateQuestion(question: QuestionEntity) = dao.updateQuestion(question)
    suspend fun deleteQuestion(question: QuestionEntity) = dao.deleteQuestion(question)

    // --- Máy chủ AI ---
    suspend fun uploadDocuments(files: List<MultipartBody.Part>): Response<OcrResponse> = aiApi.uploadDocuments(files)
    suspend fun generateQuestionsStream(request: GenerateQuestionsRequest): Response<ResponseBody> =
        aiApi.generateQuestionsStream(request)
}
