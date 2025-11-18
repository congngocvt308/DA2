package com.example.myapplication.ui.theme.alarm

import AlarmCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.MoreVert

// --- MÀN HÌNH TAB BÁO THỨC HOÀN CHỈNH (DÙNG BOX ĐỂ XẾP CHỒNG) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    alarmData: List<AlarmData>,
    onToggle: (index: Int, state: Boolean) -> Unit,
    onAlarmCardClick: (AlarmData) -> Unit
) {
    // 🚨 1. Dùng Box làm gốc để xếp chồng nội dung và FAB
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // 2. NỘI DUNG CHÍNH (Column) - Xử lý padding trên
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Xử lý khoảng đệm cho Thanh Trạng thái (pin/giờ)
//                .statusBarsPadding()
        ) {

            // a. TOP BAR CONTENT (Header của Tab)
            TopBarContent()

            // b. HEADER TEXT ("Đổ chuông sau...")
            Text(
                text = "Đổ chuông sau 9 giờ",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    // Giảm padding để Text không bị đẩy quá xa
                    .padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            )

            // c. LIST
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), // Chiếm hết không gian còn lại
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Items của bạn
                itemsIndexed(
                    items = alarmData,
                    key = { index, alarm -> alarm.id })
                { index, alarm ->
                    AlarmCard(
                        alarmData = alarm,
                        onToggle = { newCheckedState ->
                            onToggle(index, newCheckedState)
                        },
                        onCardClick = {
                            onAlarmCardClick(alarm)
                        }
                    )
                }
            }
        }

        // 3. FLOATING ACTION BUTTON (Đặt thủ công trong Box)
        FloatingActionButton(
            onClick = { /* TODO: Xử lý hiện menu con */ },
            containerColor = Color(0xFFE50043),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd) // Căn góc phải dưới
                // 🚨 Xử lý padding để né Thanh Điều hướng Hệ thống 🚨
                .navigationBarsPadding()
                .padding(16.dp) // Padding xung quanh FAB
                .size(56.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Thêm báo thức",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// --- TOP BAR CONTENT (Đã sửa lại thành Row, không phải TopAppBar của Scaffold) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent() {
    // Dùng Row để căn chỉnh tiêu đề và icon (không phải TopAppBar của Scaffold)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1C1E)) // Đảm bảo nền đen đồng nhất
            .padding(horizontal = 16.dp, vertical = 8.dp), // Padding gọn gàng
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Thiết lập báo thức",
            fontSize = 20.sp, // Cỡ chữ chuẩn
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { /* Xử lý bấm menu */ }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Tùy chọn",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}