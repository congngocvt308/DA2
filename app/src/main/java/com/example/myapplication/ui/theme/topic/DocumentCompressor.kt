package com.example.myapplication.ui.theme.topic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import com.example.myapplication.data.CompressedFileData
import com.example.myapplication.data.SelectedDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

class DocumentCompressor(private val context: Context) {

//    suspend fun compressMultipleDocuments(documents: List<SelectedDocument>): List<File> = withContext(Dispatchers.IO) {
//        val compressedFiles = mutableListOf<File>()
//
//        val publicPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//        val myAppDir = File(publicPicturesDir, "AiOcrTest")
//        if (!myAppDir.exists()) myAppDir.mkdirs()
//
//        for (doc in documents) {
//            if (doc.isPdf) {
//                // 🌟 ĐƯA TẤT CẢ PDF VỀ CHUNG LUỒNG QUẢN LÝ KHOẢNG TRANG
//                var pfd: ParcelFileDescriptor? = null
//                var renderer: PdfRenderer? = null
//                try {
//                    pfd = context.contentResolver.openFileDescriptor(doc.uri, "r")
//                    if (pfd != null) {
//                        renderer = PdfRenderer(pfd)
//
//                        if (doc.pageConfig == "Tất cả") {
//                            // Nếu chọn tất cả VÀ là file text siêu nhẹ (<1.5MB tổng) thì mới gửi nguyên bản file gốc
//                            if (doc.isPdfText && (context.contentResolver.openAssetFileDescriptor(doc.uri, "r")?.use { it.length } ?: 0L) < 1.5 * 1024 * 1024) {
//                                copyPdfRaw(doc.uri, doc.name, myAppDir)?.let { compressedFiles.add(it) }
//                            } else {
//                                // Ngược lại, duyệt nén toàn bộ trang thành ảnh
//                                for (pageIndex in 0 until renderer.pageCount) {
//                                    compressSinglePdfPage(renderer, pageIndex, doc.name, myAppDir)?.let {
//                                        compressedFiles.add(it)
//                                    }
//                                }
//                            }
//                        } else {
//                            // Người dùng đã chủ động chọn khoảng trang lẻ (Ví dụ: "1-3")
//                            val rangeParts = doc.pageConfig.split("-")
//                            val startPage = rangeParts.getOrNull(0)?.toIntOrNull()?.minus(1) ?: 0
//                            val endPage = rangeParts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: (renderer.pageCount - 1)
//
//                            val safeStart = startPage.coerceIn(0, renderer.pageCount - 1)
//                            val safeEnd = endPage.coerceIn(safeStart, renderer.pageCount - 1)
//
//                            // Duyệt trích xuất chính xác khoảng trang được yêu cầu
//                            for (pageIndex in safeStart..safeEnd) {
//                                compressSinglePdfPage(renderer, pageIndex, doc.name, myAppDir)?.let {
//                                    compressedFiles.add(it)
//                                }
//                            }
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                } finally {
//                    try { renderer?.close() } catch (_: Exception) {}
//                    try { pfd?.close() } catch (_: Exception) {}
//                }
//            } else {
//                // NHÁNH 3: HÌNH ẢNH GIỮ NGUYÊN
//                compressImage(doc.uri, doc.name, myAppDir)?.let { compressedFiles.add(it) }
//            }
//        }
//        return@withContext compressedFiles
//    }

//    private suspend fun compressImage(uri: Uri, fileName: String, outputDir: File): File? = withContext(Dispatchers.IO) {
//        try {
//            val inputStream = context.contentResolver.openInputStream(uri)
//            val originalBitmap = BitmapFactory.decodeStream(inputStream)
//            inputStream?.close()
//
//            if (originalBitmap == null) return@withContext null
//            val cleanedName = fileName.removeSuffix(".jpg").removeSuffix(".png").removeSuffix(".jpeg")
//            return@withContext saveAndCompressBitmap(originalBitmap, "img_$cleanedName", outputDir)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    private fun compressSinglePdfPage(renderer: PdfRenderer, pageIndex: Int, fileName: String, outputDir: File): File? {
//        var page: PdfRenderer.Page? = null
//        try {
//            page = renderer.openPage(pageIndex)
//            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
//
//            val canvas = android.graphics.Canvas(bitmap)
//            canvas.drawColor(android.graphics.Color.WHITE)
//            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
//
//            val cleanedName = fileName.removeSuffix(".pdf").removeSuffix(".PDF")
//            return saveAndCompressBitmap(bitmap, "pdf_${cleanedName}_page_${pageIndex + 1}", outputDir)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        } finally {
//            try { page?.close() } catch (_: Exception) {}
//        }
//    }
//
//    private fun copyPdfRaw(uri: Uri, fileName: String, outputDir: File): File? {
//        try {
//            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
//            val targetFile = File(outputDir, "RAW_TEXT_PDF_${System.currentTimeMillis()}_$fileName")
//            targetFile.outputStream().use { output ->
//                inputStream.copyTo(output)
//            }
//            // Kích hoạt quét file hệ thống để hiển thị công khai ngay lập tức
//            android.media.MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("application/pdf"), null)
//            return targetFile
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }
//    }
//
//    private fun saveAndCompressBitmap(bitmap: Bitmap, prefix: String, outputDir: File): File? {
//        try {
//            val maxDimension = 1200
//            val width = bitmap.width
//            val height = bitmap.height
//
//            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
//                val scaleFactor = maxDimension.toFloat() / max(width, height)
//                Bitmap.createScaledBitmap(bitmap, (width * scaleFactor).toInt(), (height * scaleFactor).toInt(), true)
//            } else {
//                bitmap
//            }
//
//            val outputStream = ByteArrayOutputStream()
//            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
//            val byteArray = outputStream.toByteArray()
//
//            if (scaledBitmap != bitmap) scaledBitmap.recycle()
//            bitmap.recycle()
//
//            val targetFile = File(outputDir, "COMPRESSED_${prefix}_${System.currentTimeMillis()}.jpg")
//            FileOutputStream(targetFile).use { it.write(byteArray) }
//
//            android.media.MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("image/jpeg"), null)
//            return targetFile
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return null
//        }
//    }

    suspend fun compressMultipleDocuments(documents: List<SelectedDocument>): List<CompressedFileData> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<CompressedFileData>()

        for (doc in documents) {
            if (doc.isPdf) {
                var pfd: ParcelFileDescriptor? = null
                var renderer: PdfRenderer? = null
                try {
                    pfd = context.contentResolver.openFileDescriptor(doc.uri, "r")
                    if (pfd != null) {
                        renderer = PdfRenderer(pfd)

                        // THÀNH PHẦN PDF TEXT: Gửi mảng byte nguyên bản của file thô
                        if (doc.pageConfig == "Tất cả" && doc.isPdfText) {
                            val bytes = readRawBytes(doc.uri)
                            if (bytes != null) {
                                resultList.add(CompressedFileData(doc.name, bytes, true))
                            }
                        } else {
                            // THÀNH PHẦN PDF SCAN HOẶC CHỌN KHOẢNG TRANG LẺ
                            val rangeParts = doc.pageConfig.split("-")
                            val startPage = rangeParts.getOrNull(0)?.toIntOrNull()?.minus(1) ?: 0
                            val endPage = rangeParts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: (renderer.pageCount - 1)

                            val safeStart = startPage.coerceIn(0, renderer.pageCount - 1)
                            val safeEnd = endPage.coerceIn(safeStart, renderer.pageCount - 1)

                            for (pageIndex in safeStart..safeEnd) {
                                compressSinglePdfPage(renderer, pageIndex, doc.name)?.let {
                                    resultList.add(it)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try { renderer?.close() } catch (_: Exception) {}
                    try { pfd?.close() } catch (_: Exception) {}
                }
            } else {
                // HÌNH ẢNH ĐỒ HỌA GỐC
                compressImage(doc.uri, doc.name)?.let { resultList.add(it) }
            }
        }
        return@withContext resultList
    }

    private fun compressSinglePdfPage(renderer: PdfRenderer, pageIndex: Int, fileName: String): CompressedFileData? {
        var page: PdfRenderer.Page? = null
        try {
            page = renderer.openPage(pageIndex)
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            val cleanedName = fileName.removeSuffix(".pdf").removeSuffix(".PDF")
            val bytes = processBitmapToBytes(bitmap) ?: return null
            return CompressedFileData("pdf_${cleanedName}_page_${pageIndex + 1}.jpg", bytes, false)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try { page?.close() } catch (_: Exception) {}
        }
    }

    private suspend fun compressImage(uri: Uri, fileName: String): CompressedFileData? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return@withContext null
            val cleanedName = fileName.removeSuffix(".jpg").removeSuffix(".png").removeSuffix(".jpeg")
            val bytes = processBitmapToBytes(originalBitmap) ?: return@withContext null
            return@withContext CompressedFileData("img_$cleanedName.jpg", bytes, false)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun processBitmapToBytes(bitmap: Bitmap): ByteArray? {
        try {
            val maxDimension = 1200
            val width = bitmap.width
            val height = bitmap.height

            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val scaleFactor = maxDimension.toFloat() / max(width, height)
                Bitmap.createScaledBitmap(bitmap, (width * scaleFactor).toInt(), (height * scaleFactor).toInt(), true)
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
            val byteArray = outputStream.toByteArray()

            if (scaledBitmap != bitmap) scaledBitmap.recycle()
            bitmap.recycle()

            return byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun readRawBytes(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}