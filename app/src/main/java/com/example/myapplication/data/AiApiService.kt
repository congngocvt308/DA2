package com.example.myapplication.data

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming

interface AiApiService {
    @Multipart
    @POST("api/ai/analyze") // Đường dẫn Endpoint tại Backend của bạn
    suspend fun uploadDocuments(
        @Part files: List<MultipartBody.Part>
    ): Response<OcrResponse>

    @POST("api/ai/generate-questions")
    @Streaming
    suspend fun generateQuestionsStream(
        @Body request: GenerateQuestionsRequest
    ): Response<ResponseBody>
}