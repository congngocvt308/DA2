package com.cong.smartalarm.feature.smile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Chụp ảnh camera trước, lật gương và lưu vào thư mục ẩn alarm_photos của app. */
fun takePhotoToInternalStorage(
    context: Context,
    imageCapture: ImageCapture,
    onPhotoSaved: (imageUri: String, description: String) -> Unit
) {
    // 1. Tạo thư mục ẩn riêng trong App
    val photoFolder = File(context.filesDir, "alarm_photos").apply {
        if (!exists()) mkdirs()
    }

    // 2. Tạo file .nomedia chặn Gallery ứng dụng khác quét
    val noMediaFile = File(photoFolder, ".nomedia")
    if (!noMediaFile.exists()) {
        try { noMediaFile.createNewFile() } catch (e: Exception) { e.printStackTrace() }
    }

    val now = Date()
    // 🎯 3. Định dạng tên file theo chuẩn "Giờ:Phút Ngày-Tháng" (Ví dụ: 15:30 25-07.jpg)
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now)
    val photoFile = File(photoFolder, "IMG_$timeStamp.jpg")

    // 🎯 2. Tên mặc định hiển thị trên Card/Dialog chỉ có "Giờ:Phút" (Ví dụ: 11:16)
    val displayDescription = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)

    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                // 1. Chuyển ImageProxy thành Bitmap
                val bitmap = imageProxyToBitmap(image)
                image.close()

                // 2. Lật ngang Bitmap (Flip Horizontal) cho đúng góc nhìn gương
                val matrix = android.graphics.Matrix().apply {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
                val flippedBitmap = android.graphics.Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )

                // 3. Ghi Bitmap đã lật vào File mang tên vừa đặt
                FileOutputStream(photoFile).use { out ->
                    flippedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }

                val savedUri = photoFile.absolutePath

                onPhotoSaved(savedUri, displayDescription)

                Toast.makeText(context, "Ảnh đã được lưu vào Bảo Tàng Nụ Cười ✨", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraCapture", "Lỗi chụp ảnh: ${exception.message}", exception)
            }
        }
    )
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}