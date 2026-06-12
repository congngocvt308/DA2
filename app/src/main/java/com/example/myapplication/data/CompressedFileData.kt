package com.example.myapplication.data

data class CompressedFileData(
    val fileName: String,
    val bytes: ByteArray,
    val isRawPdf: Boolean
)
