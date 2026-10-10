package com.cong.smartalarm.core.database.model

data class TopicData(
    val id: Int,
    val name: String,
    val questionCount: Int
)

data class TopicWithCountResult(
    val topicId: Int,
    val topicName: String,
    val questionCount: Int
)