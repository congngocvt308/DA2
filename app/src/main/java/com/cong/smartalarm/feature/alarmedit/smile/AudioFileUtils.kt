package com.cong.smartalarm.feature.alarmedit.smile

import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

/**
 * Hàm hỗ trợ sao chép tệp từ Uri vào bộ nhớ trong của ứng dụng
 */
internal fun copyUriToInternalStorage(context: android.content.Context, uri: Uri): String? {
    val contentResolver = context.contentResolver
    // Đảm bảo tên file copy vào luôn là duy nhất bằng cách chèn timestamp vào trước tên gốc
    val originalName = getFileName(context, uri) ?: "sound"
    val fileName = "file_${System.currentTimeMillis()}_$originalName"
    val outputFile = File(context.filesDir, fileName)

    return try {
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(outputFile)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
    }
    if (name == null) {
        name = uri.path
        val cut = name?.lastIndexOf('/') ?: -1
        if (cut != -1) name = name?.substring(cut + 1)
    }
    return name
}

internal fun getAudioDuration(context: android.content.Context, uri: Uri): Long {
    val retriever = android.media.MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
        time?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}

// Overload cho đường dẫn file string
internal fun getAudioDuration(path: String): Long {
    val retriever = android.media.MediaMetadataRetriever()
    return try {
        retriever.setDataSource(path)
        val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
        time?.toLong() ?: 0L
    } catch (e: Exception) {
        0L
    } finally {
        retriever.release()
    }
}
