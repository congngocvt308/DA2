package com.example.myapplication.data

import androidx.room.Embedded
import androidx.room.Relation

data class TopicWithQuestions(
    @Embedded val topic: TopicEntity,
    @Relation(
        parentColumn = "topicId",
        entityColumn = "ownerTopicId"
    )
    val questions: List<QuestionEntity>
)