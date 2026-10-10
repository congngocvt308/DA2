package com.cong.smartalarm.feature.topic.model

data class CompressedFileData(
    val fileName: String,
    val bytes: ByteArray,
    val isRawPdf: Boolean
)
