package com.cong.smartalarm.feature.alarmedit.smile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cong.smartalarm.core.designsystem.component.CalmButton
import com.cong.smartalarm.feature.smile.CustomPaintProgressBar
import java.io.File
import java.io.FileOutputStream

/** Chỉnh vị trí/zoom hình nền trên khung xem trước màn thử thách nụ cười. */
@Composable
fun BackgroundEditorDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    BackgroundEditorContent(imageUri, onDismiss, onConfirm)
}

@Composable
private fun BackgroundEditorContent(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val context = LocalContext.current

    // Load bitmap gốc để xử lý zoom/pan không bị cắt
    val originalBitmap = remember(imageUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) { null }
    }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onGloballyPositioned { containerSize = it.size }
        ) {
            // 1. ẢNH NỀN TỰ DO (Dùng Canvas vẽ trực tiếp Bitmap)
            if (originalBitmap != null) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale *= zoom
                                offset += pan
                            }
                        }
                ) {
                    drawIntoCanvas { canvas ->
                        val matrix = Matrix()
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val bitmapWidth = originalBitmap.width.toFloat()
                        val bitmapHeight = originalBitmap.height.toFloat()

                        // Khởi tạo Fit màn hình (Fill Center)
                        val baseScale = maxOf(canvasWidth / bitmapWidth, canvasHeight / bitmapHeight)

                        matrix.postTranslate(-bitmapWidth / 2f, -bitmapHeight / 2f)
                        matrix.postScale(baseScale * scale, baseScale * scale)
                        matrix.postTranslate(canvasWidth / 2f + offset.x, canvasHeight / 2f + offset.y)

                        canvas.nativeCanvas.drawBitmap(originalBitmap, matrix, null)
                    }
                }
            }

            // 2. OVERLAY TĨNH (NHƯ FACE MISSION SCREEN)
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 50.dp, bottom = 40.dp, start = 32.dp, end = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Clock
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("07:30", color = Color.White.copy(alpha = 0.9f), fontSize = 76.sp, fontWeight = FontWeight.Bold)
                    Text("Thứ Hai, 24 tháng 7", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(80.dp))

                // Khung Camera Tĩnh
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.9f)
                        .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(40.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("XEM TRƯỚC VỊ TRÍ MẶT", color = Color.White, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Progress Bar Tĩnh
                Text("Cười tươi để kiểm tra", color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                CustomPaintProgressBar(progress = 0.6f, modifier = Modifier.fillMaxWidth().height(74.dp))
            }

            // 3. NÚT BACK (Góc Top Left)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(top = 40.dp, start = 16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }

            // 4. NÚT LƯU (Dưới cùng)
            CalmButton(
                onClick = {
                    if (originalBitmap != null && containerSize != IntSize.Zero) {
                        val resultPath = saveTransformedBitmap(context, originalBitmap, scale, offset, containerSize)
                        if (resultPath != null) onConfirm(resultPath)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Lưu vị trí")
            }
        }
    }
}

private fun saveTransformedBitmap(
    context: android.content.Context,
    original: Bitmap,
    userScale: Float,
    userOffset: Offset,
    screenSize: IntSize
): String? {
    try {
        val output = Bitmap.createBitmap(screenSize.width, screenSize.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val matrix = Matrix()

        val baseScale = maxOf(screenSize.width.toFloat() / original.width, screenSize.height.toFloat() / original.height)

        matrix.postTranslate(-original.width / 2f, -original.height / 2f)
        matrix.postScale(baseScale * userScale, baseScale * userScale)
        matrix.postTranslate(screenSize.width / 2f + userOffset.x, screenSize.height / 2f + userOffset.y)

        val paint = android.graphics.Paint().apply {
            isFilterBitmap = true
        }
        canvas.drawBitmap(original, matrix, paint)

        val file = File(context.filesDir, "bg_custom_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use {
            output.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }
        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
