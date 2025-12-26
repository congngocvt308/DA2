package com.example.myapplication.data

data class DailyStat(
    val day: String,   // Kết quả từ hàm date() trong SQL
    val correct: Int,  // Kết quả từ hàm SUM()
    val total: Int     // Kết quả từ hàm COUNT()
)

// 2. Dùng để hứng dữ liệu phân phối SRS
data class SrsStat(
    val status: String, // 'New', 'Learning', hoặc 'Mastered'
    val count: Int      // Số lượng câu hỏi tương ứng
)
