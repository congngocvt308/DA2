package com.example.myapplication.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri

class SoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    // Biến lưu URI của bài hát đang phát để so sánh
    private var currentPlayingUri: String? = null

    fun playOrUpdateVolume(uriString: String, volume: Float) {
        if (uriString.isBlank()) return

        // TRƯỜNG HỢP 1: Bài này đang phát rồi -> Chỉ chỉnh Volume
        if (mediaPlayer != null && mediaPlayer!!.isPlaying && currentPlayingUri == uriString) {
            mediaPlayer?.setVolume(volume, volume)
            return
        }

        // TRƯỜNG HỢP 2: Chưa phát hoặc đổi bài khác -> Phát mới
        stop() // Dừng bài cũ (nếu có)

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(uriString))
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setVolume(volume, volume)
                prepare()
                start()

                // Khi hát xong thì reset biến
                setOnCompletionListener {
                    currentPlayingUri = null
                }
            }
            // Lưu lại URI bài đang phát
            currentPlayingUri = uriString

        } catch (e: Exception) {
            e.printStackTrace()
            playDefault()
        }
    }

    private fun playDefault() {
        try {
            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, defaultUri)
                prepare()
                start()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }


    fun stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaPlayer = null
            currentPlayingUri = null
        }
    }
}