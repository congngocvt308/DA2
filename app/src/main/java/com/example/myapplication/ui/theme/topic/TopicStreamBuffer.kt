package com.example.myapplication.ui.theme.topic

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