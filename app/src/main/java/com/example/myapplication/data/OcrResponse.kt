package com.example.myapplication.data

import com.google.gson.annotations.SerializedName

data class OcrResponse(
    @SerializedName("suggested_topic")
    val suggestedTopic: String,

    @SerializedName("extracted_text")
    val extractedLatexContent: String
)