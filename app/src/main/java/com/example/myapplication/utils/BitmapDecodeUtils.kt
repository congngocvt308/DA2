package com.example.myapplication.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream
import kotlin.math.max

object BitmapDecodeUtils {
    private const val MAX_EDGE_PX = 1280

    fun decodeSampledBitmap(inputStream: InputStream): Bitmap? {
        val bytes = inputStream.readBytes()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        val sampleSize = calculateInSampleSize(bounds, MAX_EDGE_PX, MAX_EDGE_PX)
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (
                halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }
}
