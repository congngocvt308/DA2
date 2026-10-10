package com.cong.smartalarm.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkConfig {
    /** Máy chủ AI trong mạng nội bộ — đổi thành IP máy chạy server của bạn. */
    const val AI_BASE_URL = "http://192.168.1.219:3000/"

    /** Phân tích tài liệu và sinh câu hỏi có thể chạy lâu. */
    const val TIMEOUT_SECONDS = 60L
}

/**
 * Tạo client mạng một lần cho cả app (AppContainer giữ lại):
 * OkHttpClient dùng chung giữ được pool kết nối và luồng, thay vì dựng mới mỗi lần gọi API.
 */
object NetworkModule {
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(NetworkConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    fun aiApiService(client: OkHttpClient): AiApiService = Retrofit.Builder()
        .baseUrl(NetworkConfig.AI_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AiApiService::class.java)
}
