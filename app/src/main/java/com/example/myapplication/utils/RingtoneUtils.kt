package com.example.myapplication.utils

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns

data class RingtoneItem(val title: String, val uri: String)

object RingtoneUtils {
    // Lấy danh sách tất cả nhạc chuông báo thức
    fun getRingtoneList(context: Context): List<RingtoneItem> {
        val list = mutableListOf<RingtoneItem>()
        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = manager.cursor

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uriString = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
                val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
                val fullUri = "$uriString/$id"
                list.add(RingtoneItem(title, fullUri))
            } while (cursor.moveToNext())
        }
        return list
    }

    // Hàm lấy tên bài hát từ URI (để hiển thị lên màn hình chính)
    fun getRingtoneTitle(context: Context, uriString: String): String {
        if (uriString.isBlank()) return "Mặc định"
        val uri = Uri.parse(uriString)

        try {
            // 1. Thử lấy tên theo kiểu Nhạc Chuông Hệ Thống
            val ringtone = RingtoneManager.getRingtone(context, uri)
            val title = ringtone?.getTitle(context)

            // Nếu lấy được tên và trông nó "xịn" (không phải là uri string) thì trả về
            if (title != null && !title.contains("content://")) {
                return title
            }
        } catch (e: Exception) {
            // Bỏ qua lỗi
        }

        // 2. Nếu cách 1 thất bại (hoặc là file MP3 chọn từ bộ nhớ), dùng ContentResolver
        return getFileNameFromCursor(context, uri) ?: "File âm thanh"
    }

    // Hàm phụ trợ để đọc tên file từ bộ nhớ (Phiên bản nâng cấp)
    private fun getFileNameFromCursor(context: Context, uri: Uri): String? {
        var result: String? = null

        // 1. Thử truy vấn thông tin file từ ContentResolver
        if (uri.scheme == "content") {
            // query(uri, projection, ...) với projection là null để lấy tất cả cột
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    // Cố gắng tìm cột Tên Hiển Thị chuẩn
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }

        // 2. Nếu cách 1 thất bại (result vẫn null), thử cách cắt đường dẫn
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        // 3. Xử lý trường hợp tên bị dính mã lạ (như "msf:35")
        // Nếu tên quá ngắn hoặc trông giống ID, ta có thể thử "decode" nó (nâng cao),
        // nhưng thường bước 1 ở trên đã giải quyết được 99% trường hợp.

        return result
    }
}