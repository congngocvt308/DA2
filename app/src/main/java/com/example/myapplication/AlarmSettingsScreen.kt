package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Màn hình cài đặt báo thức chính (Phiên bản dùng Box)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingScreen() {
    // --- State cho các thành phần ---
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(10) }

    val daysOfWeek = remember {
        mutableStateMapOf(
            "CN" to false, "T2" to true, "T3" to true, "T4" to true, "T5" to true, "T6" to true, "T7" to false
        )
    }
    var repeatDaily by remember { mutableStateOf(true) }

    var volume by remember { mutableStateOf(0.7f) }
    var gentleWakeup by remember { mutableStateOf(true) }
    var timePressure by remember { mutableStateOf(false) }
    var weatherReminder by remember { mutableStateOf(true) }

    // --- State cho 2 LazyColumn (theo yêu cầu của bạn) ---
    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedHour)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedMinute)

    // --- Sử dụng Box làm gốc để nút "Lưu" nổi lên ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // Đặt màu nền cho toàn màn hình
    ) {

        // --- NỘI DUNG (Bao gồm TopAppBar và LazyColumn) ---
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. TopAppBar (thêm thủ công vì không dùng Scaffold)
            CenterAlignedTopAppBar(
                title = { Text("Chuông báo thức") },
                navigationIcon = {
                    IconButton(onClick = { /* Xử lý back */ }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // 2. Danh sách cuộn các cài đặt
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Chiếm hết không gian còn lại
                horizontalAlignment = Alignment.CenterHorizontally,
                // Thêm padding cuối để nội dung không bị nút "Lưu" che
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 100.dp // Đủ không gian cho nút và padding
                )
            ) {

                // --- 1. PHẦN CHỈNH THỜI GIAN (2 LazyColumn) ---
                item {
                    TimePickerSection(
                        hourListState = hourListState,
                        minuteListState = minuteListState,
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute
                    )
                }

                // --- 2. ĐỔ CHUÔNG SAU ---
                item {
                    Text(
                        text = "Đổ chuông sau 17 giờ 51 phút.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // --- 3. CHỌN NGÀY ---
                item {
                    DaySelectorSection(
                        daysOfWeek = daysOfWeek,
                        repeatDaily = repeatDaily,
                        onRepeatDailyChange = { repeatDaily = it }
                    )
                }

                // --- 4. NHIỆM VỤ BÁO THỨC ---
                item {
                    SettingsSectionHeader(title = "Nhiệm vụ báo thức")
                    AlarmTaskSection()
                }

                // --- 5. ÂM THANH BÁO THỨC ---
                item {
                    SettingsSectionHeader(title = "Âm thanh báo thức")
                    SoundSelectionRow()
                    VolumeSliderRow(volume = volume, onVolumeChange = { volume = it })
                }

                // --- 6. CÁC TÙY CHỌN (TOGGLE) ---
                item {
                    SettingsNavigationItem(
                        title = "Thức giấc nhẹ nhàng",
                        value = "30 giây",
                        onClick = { /* Mở màn hình chọn 30 giây */ }
                    )
                    SettingsToggleItem(
                        title = "Áp lực thời gian",
                        checked = timePressure,
                        onCheckedChange = { timePressure = it }
                    )
                    SettingsToggleItem(
                        title = "Lời nhắc thời tiết",
                        checked = weatherReminder,
                        onCheckedChange = { weatherReminder = it },
                        isNew = true
                    )
                    SettingsToggleItem(
                        title = "Lời nhắc nhãn",
                        checked = false,
                        onCheckedChange = {},
                        locked = true
                    )
                    SettingsToggleItem(
                        title = "Âm thanh dự phòng",
                        checked = false,
                        onCheckedChange = {},
                        locked = true
                    )
                }

                // --- 7. CÀI ĐẶT TÙY CHỈNH ---
                item {
                    SettingsSectionHeader(title = "Cài đặt tùy chỉnh")
                    SettingsNavigationItem(
                        title = "Báo lại",
                        value = "5 phút, Vô hạn",
                        onClick = { /* Mở cài đặt báo lại */ }
                    )
                    WallpaperPreviewRow()
                }
            }
        }

        // --- Nút "Lưu" nổi ở dưới cùng ---
        Button(
            onClick = { /* Xử lý lưu */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Padding xung quanh nút
                .height(56.dp)
                .align(Alignment.BottomCenter), // Căn chỉnh nút xuống đáy Box
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)), // Màu đỏ
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Lưu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- CÁC COMPOSABLE CON (HELPER FUNCTIONS) ---

/**
 * Phần chọn thời gian sử dụng 2 LazyColumn.
 */
@Composable
fun TimePickerSection(
    hourListState: LazyListState,
    minuteListState: LazyListState,
    selectedHour: Int,
    selectedMinute: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Cố định chiều cao để tạo cửa sổ cuộn
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cột Giờ
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = hourListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            // Thêm padding để item đầu và cuối có thể cuộn vào giữa
            contentPadding = PaddingValues(vertical = 60.dp)
        ) {
            items(24) { hour ->
                Text(
                    text = "%02d".format(hour),
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (hour == selectedHour) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Cột Phút
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = minuteListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 60.dp)
        ) {
            items(60) { minute ->
                Text(
                    text = "%02d".format(minute),
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (minute == selectedMinute) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Phần chọn ngày trong tuần
 */
/**
 * Phần chọn ngày trong tuần
 * *** ĐÃ SẮP XẾP LẠI THỨ TỰ NGÀY ***
 */
@Composable
fun DaySelectorSection(
    daysOfWeek: SnapshotStateMap<String, Boolean>,
    repeatDaily: Boolean,
    onRepeatDailyChange: (Boolean) -> Unit
) {
    // --- Lấy màu sắc từ theme M3 ---
    val selectedColor = MaterialTheme.colorScheme.primary
    val onSelectedColor = MaterialTheme.colorScheme.onPrimary
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant
    val onUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- Phần Checkbox "Hàng ngày" (giữ nguyên) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRepeatDailyChange(!repeatDaily) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hàng ngày", style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = repeatDaily, onCheckedChange = onRepeatDailyChange)
        }

        // --- SỬA ĐỔI CHÍNH: Tạo một List có thứ tự mong muốn ---
        val dayOrder = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

        // --- Đã đổi sang Row ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Thêm chút padding
            // Dùng SpaceAround để 7 item tự động căn đều
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Dùng forEach trên 'dayOrder' thay vì 'daysOfWeek.keys'
            dayOrder.forEach { day ->
                // Lấy trạng thái (isSelected) từ Map,
                // nhưng vẫn giữ thứ tự của 'dayOrder'
                val isSelected = daysOfWeek[day] ?: false

                // Gọi Composable "tự chế" của chúng ta
                CustomDayChip(
                    text = day,
                    isSelected = isSelected,
                    selectedColor = selectedColor,
                    onSelectedColor = onSelectedColor,
                    unselectedColor = unselectedColor,
                    onUnselectedColor = onUnselectedColor,
                    onClick = { daysOfWeek[day] = !isSelected }
                )
            }
        }
    }
}

@Composable
fun CustomDayChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onSelectedColor: Color,
    unselectedColor: Color,
    onUnselectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        // Căn giữa Text bên trong Box
        contentAlignment = Alignment.Center,
        modifier = Modifier
            // 1. Hình dạng: Bo góc 8.dp (giống ảnh)
            .clip(RoundedCornerShape(8.dp))
            // 2. Màu nền: Thay đổi dựa trên isSelected
            .background(
                if (isSelected) selectedColor else unselectedColor
            )
            // 3. Xử lý click
            .clickable { onClick() }
            // 4. Kích thước & Padding (có thể điều chỉnh)
            .sizeIn(minWidth = 48.dp, minHeight = 40.dp) // Đặt kích thước tối thiểu
            .padding(horizontal = 12.dp) // Padding cho Text
    ) {
        Text(
            text = text,
            // 5. Màu chữ: Thay đổi dựa trên isSelected
            color = if (isSelected) onSelectedColor else onUnselectedColor,
            style = MaterialTheme.typography.bodyMedium // (Tùy chỉnh)
        )
    }
}

/**
 * Phần "Nhiệm vụ báo thức"
 */
@Composable
fun AlarmTaskSection() {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Task 1: (Ví dụ)
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Text("5 lần", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
        // Các task bị khóa (Ví dụ)
        items(3) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Khóa", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("...", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * Hàng chọn nhạc
 */
@Composable
fun SoundSelectionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Mở danh sách nhạc */ }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
        Text(
            text = "TOKUSOU SENTAI DEKAR...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.NavigateNext, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Hàng thanh trượt âm lượng
 */
@Composable
fun VolumeSliderRow(volume: Float, onVolumeChange: (Float) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.VolumeUp, contentDescription = "Âm lượng")
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Tiêu đề cho các phần (Section)
 */
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
    )
}

/**
 * Một hàng item có công tắc (Switch/Toggle)
 */
@Composable
fun SettingsToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isNew: Boolean = false,
    locked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !locked) { onCheckedChange(!checked) }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (isNew) {
                Text(
                    text = "NEW",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            if (locked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Khóa",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = !locked
        )
    }
}

/**
 * Một hàng item dùng để điều hướng (ví dụ: "Báo lại")
 */
@Composable
fun SettingsNavigationItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                Icons.Default.NavigateNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Hàng xem trước hình nền
 */
@Composable
fun WallpaperPreviewRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Mở chọn hình nền */ }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Cài đặt hình nền", style = MaterialTheme.typography.bodyLarge)

        // Box dùng để giả lập hình nền
        Box(
            modifier = Modifier
                .size(width = 60.dp, height = 80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFA726))
                    )
                )
        )
    }
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // Thêm một MaterialTheme giả để preview hoạt động
    // (Trong ứng dụng thật, bạn sẽ dùng Theme chính của mình)
    MaterialTheme {
        AlarmSettingScreen()
    }
}