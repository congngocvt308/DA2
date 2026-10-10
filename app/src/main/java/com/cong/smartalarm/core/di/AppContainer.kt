package com.cong.smartalarm.core.di

import android.content.Context
import com.cong.smartalarm.SmartAlarmApp
import com.cong.smartalarm.core.alarm.AlarmScheduler
import com.cong.smartalarm.core.database.AppDatabase
import com.cong.smartalarm.core.network.AiApiService
import com.cong.smartalarm.core.network.NetworkModule
import com.cong.smartalarm.data.repository.AlarmRepository
import com.cong.smartalarm.data.repository.LearningRepository
import com.cong.smartalarm.data.repository.PictureRepository
import com.cong.smartalarm.data.repository.QRCodeRepository
import com.cong.smartalarm.data.repository.StatsRepository
import com.cong.smartalarm.data.repository.TopicRepository
import com.cong.smartalarm.domain.SrsEngine
import okhttp3.OkHttpClient

/**
 * Nơi duy nhất tạo các phụ thuộc dùng chung (DI thủ công, không cần Hilt).
 * Mọi thứ khởi tạo lười (lazy) và sống cùng Application: một database, một OkHttpClient,
 * một AiApiService. ViewModel nhận repository qua Factory thay vì tự gọi AppDatabase.getDatabase().
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.getDatabase(appContext) }

    private val okHttpClient: OkHttpClient by lazy { NetworkModule.okHttpClient() }
    private val aiApiService: AiApiService by lazy { NetworkModule.aiApiService(okHttpClient) }

    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(appContext) }

    val alarmRepository by lazy { AlarmRepository(database.appDao()) }
    val topicRepository by lazy { TopicRepository(database.appDao(), aiApiService) }
    val qrCodeRepository by lazy { QRCodeRepository(database.appDao()) }
    val pictureRepository by lazy { PictureRepository(database.appDao()) }
    val learningRepository by lazy { LearningRepository(database.appDao()) }
    val statsRepository by lazy { StatsRepository(database.statisticsDao()) }

    val srsEngine by lazy { SrsEngine(learningRepository) }
}

/** Lấy AppContainer từ bất kỳ Context nào (Activity, Service, Receiver, Composable). */
val Context.appContainer: AppContainer
    get() = (applicationContext as SmartAlarmApp).container
