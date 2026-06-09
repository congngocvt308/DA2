package com.example.myapplication.data

import android.net.Uri

data class SelectedDocument(
    val id: String,          // Định danh duy nhất để tránh trùng lặp khi render danh sách (dùng UUID hoặc timestamp)
    val uri: Uri,            // Đường dẫn Uri gốc từ hệ thống
    val name: String,         // Tên tệp tin hiển thị công khai
    val isPdf: Boolean,      // Phân biệt nhanh định dạng để bật/tắt UI chọn trang
    val isPdfText: Boolean = false,
    val pageConfig: String = "Tất cả" ,// Mặc định là cấu hình xử lý tất cả các trang
    val totalPages: Int = 1
)
