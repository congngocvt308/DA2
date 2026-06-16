package com.example.myapplication.ui.theme.topic

import android.annotation.SuppressLint
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathText(
    latexText: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 16,
    textColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    isBold: Boolean = false,
    textAlign: String = "left",
    maxLines: Int? = null
) {
    val colorHex = String.format("#%06X", 0xFFFFFF and textColor.toArgb())
    val fontWeight = if (isBold) "bold" else "normal"

    // Tạo style cho maxLines
    val maxLinesStyle = if (maxLines != null) """
        display: -webkit-box;
        -webkit-line-clamp: $maxLines;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all; /* Tránh tràn ngang làm mất dấu 3 chấm */
        line-height: 1.4em;
        max-height: ${maxLines * 1.4}em; /* Giới hạn chiều cao cứng */
    """.trimIndent() else ""

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    textZoom = 100
                }
                setBackgroundColor(0)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        update = { webView ->
            val processedText = if (maxLines == 1) {
                latexText.replace("$$", "$").replace("\n", " ")
            } else {
                latexText
            }

            val htmlData = """
                <!DOCTYPE html>
                <html style="margin: 0; padding: 0;">
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                    <link rel="stylesheet" href="file:///android_asset/katex/katex.min.css">
                    <script src="file:///android_asset/katex/katex.min.js"></script>
                    <script src="file:///android_asset/katex/auto-render.min.js"></script>
                    <style>
                        body { 
                            color: $colorHex; 
                            font-size: ${fontSize}px; 
                            font-weight: $fontWeight;
                            text-align: $textAlign;
                            margin: 0; 
                            padding: 0;
                            background-color: transparent;
                            $maxLinesStyle
                        }
                        #math { width: 100%; }
                        /* Thu nhỏ công thức một chút nếu nó quá cao trong chế độ 1 dòng */
                        .katex { font-size: 1em; }
                        .katex-display { margin: 0.2em 0; }
                    </style>
                </head>
                <body>
                    <div id="math">$processedText</div>
                    <script>
                        function render() {
                            renderMathInElement(document.getElementById('math'), {
                                delimiters: [
                                    {left: '$$', right: '$$', display: ${if (maxLines == 1) "false" else "true"}},
                                    {left: '$', right: '$', display: false}
                                ],
                                throwOnError: false
                            });
                        }
                        if (typeof renderMathInElement === 'function') {
                            render();
                        } else {
                            window.onload = render;
                        }
                    </script>
                </body>
                </html>
            """.trimIndent()

            webView.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null)
        }
    )
}