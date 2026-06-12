package com.example.myapplication.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AiApiService {
    @Multipart
    @POST("api/ai/analyze") // Đường dẫn Endpoint tại Backend của bạn
    suspend fun uploadDocuments(
        @Part files: List<MultipartBody.Part>
    ): Response<OcrResponse>
}