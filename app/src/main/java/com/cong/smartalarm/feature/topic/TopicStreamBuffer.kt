package com.cong.smartalarm.feature.topic

object TopicStreamBuffer {
    var pendingRequest: StreamRequest? = null

    data class StreamRequest(
        val topicId: Int,
        val extractedText: String,
        val easy: Int,
        val mid: Int,
        val hard: Int
    )
}