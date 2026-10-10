package com.cong.smartalarm.core.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    fun startRecording(fileName: String): String? {
        val file = File(context.filesDir, "$fileName.m4a")
        currentOutputFile = file
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
                Log.d("VoiceRecorder", "Started recording to: ${file.absolutePath}")
            } catch (e: IOException) {
                Log.e("VoiceRecorder", "prepare() failed", e)
                return null
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "start() failed", e)
                return null
            }
        }
        return file.absolutePath
    }

    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            val path = currentOutputFile?.absolutePath
            Log.d("VoiceRecorder", "Stopped recording. File saved at: $path")
            path
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "stopRecording failed", e)
            null
        }
    }
}
