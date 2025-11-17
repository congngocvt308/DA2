package com.example.myapplication.ui.theme.alarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.TextStyle
import kotlin.math.abs
import androidx.compose.material3.Slider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Màn hình cài đặt báo thức chính (Phiên bản dùng Box)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingScreen() {
    // --- State cho các thành phần ---
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(10) }
    var text by remember { mutableStateOf("") }
    val daysOfWeek = remember {
        mutableStateMapOf(
            "CN" to false, "T2" to true, "T3" to true, "T4" to true, "T5" to true, "T6" to true, "T7" to false
        )
    }
    var repeatDaily by remember { mutableStateOf(true) }
    var volume by remember { mutableFloatStateOf(0.7f) }

    // --- State cho 2 LazyColumn (theo yêu cầu của bạn) ---
    // 🚨 SỬA LỖI 3: FOCUS BAN ĐẦU (Ở GIỮA)
    // (Để item 8 ở giữa, item 7 phải ở trên cùng)
    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = max(0, selectedHour - 1))
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = max(0, selectedMinute - 1))


    // --- 🚨 LOGIC SNAPPING CHO GIỜ (MỚI) ---
    rememberSnapLogic(
        lazyListState = hourListState,
        onItemSelected = { newHour -> selectedHour = newHour }
    )

    // --- 🚨 LOGIC SNAPPING CHO PHÚT (MỚI) ---
    rememberSnapLogic(
        lazyListState = minuteListState,
        onItemSelected = { newMinute -> selectedMinute = newMinute }
    )

    // --- Sử dụng Box làm gốc để nút "Lưu" nổi lên ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Đặt màu nền cho toàn màn hình
    ) {

        // --- NỘI DUNG (Bao gồm TopAppBar và LazyColumn) ---
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. TopAppBar (thêm thủ công vì không dùng Scaffold)
            CenterAlignedTopAppBar(
                title = { Text(
                    text = "Chuông báo thức",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ) },
                navigationIcon = {
                    IconButton(onClick = { /* Xử lý back */ }) {
                        Icon(Icons.Default.Close,
                            tint = Color.White,
                            contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
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
                item {
                    TextField(
                        value = text,
                        onValueChange = {text = it},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp,20.dp,0.dp,0.dp),
                        placeholder = {
                            Text(
                                text = "Vui lòng điền tên báo thức",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Image(
                                painter = painterResource(R.drawable.sun),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Edit,
                                contentDescription = "Sửa tên",
                                tint = Color.Gray
                            )
                        },
                        // 🚨 SỬA LỖI 1: THÊM MÀU CHỮ VÀO ĐÂY
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,

                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,

                            cursorColor = Color.White,

                            focusedTextColor = Color.White,   // Chữ màu trắng khi gõ
                            unfocusedTextColor = Color.White, // Chữ màu trắng khi không focus

                            // Sửa 'placeholderColor' thành 2 dòng này
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        textStyle = TextStyle(fontSize = 18.sp)
                    )
                }

                // --- 1. PHẦN CHỈNH THỜI GIAN (2 LazyColumn) ---
                item {
                    TimePickerSection(
                        hourListState = hourListState,
                        minuteListState = minuteListState,
                        selectedHour = selectedHour,
                        selectedMinute = selectedMinute,
                        onHourChange = { newHour -> selectedHour = newHour },
                        onMinuteChange = { newMinute -> selectedMinute = newMinute }
                    )
                }

                //--- 2. ĐỔ CHUÔNG SAU ---
                item {
                    Text(
                        text = "Đổ chuông sau 17 giờ 51 phút.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(0.dp,20.dp,0.dp,0.dp),
                    )
                }

                //--- 3. CHỌN NGÀY ---
                item {
                    DaySelectorSection(
                        daysOfWeek = daysOfWeek,
                        repeatDaily = repeatDaily,
                        onRepeatDailyChange = { repeatDaily = it }
                    )
                }

                // --- 4. NHIỆM VỤ BÁO THỨC ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp,20.dp,0.dp,0.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ){
                            SettingsSectionHeader(title = "Nhiệm vụ báo thức")
                            AlarmTaskSection()
                        }
                    }
                }

                // --- 5. ÂM THANH BÁO THỨC ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp,20.dp,0.dp,0.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ){
                            SettingsSectionHeader(title = "Âm thanh báo thức")
                            SoundSelectionRow()
                            VolumeSliderRow(volume = volume, onVolumeChange = { volume = it })
                        }
                    }
                }

                // --- 7. CÀI ĐẶT TÙY CHỈNH ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp,20.dp,0.dp,0.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ){
                            SettingsSectionHeader(title = "Cài đặt tùy chỉnh")
                            SettingsNavigationItem(
                                title = "Báo lại",
                                value = "5 phút, Vô hạn",
                                onClick = { /* Mở cài đặt báo lại */ }
                            )
                        }
                    }
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
 * Hàm Helper để lắng nghe trạng thái cuộn và "bắt dính" (snap)
 */
@Composable
private fun rememberSnapLogic(
    lazyListState: LazyListState,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            coroutineScope.launch {
                delay(100) // Đợi cuộn quán tính kết thúc

                // Tính toán item gần nhất với vị trí trên cùng
                val firstVisibleItem = lazyListState.firstVisibleItemIndex
                val firstVisibleItemOffset = lazyListState.firstVisibleItemScrollOffset
                val itemHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 40

                val snapIndex = if (firstVisibleItemOffset > (itemHeight / 2)) {
                    firstVisibleItem + 1
                } else {
                    firstVisibleItem
                }

                // 1. Cuộn đến item trên cùng (snapIndex)
                lazyListState.animateScrollToItem(snapIndex)

                // 2. Cập nhật state (item ở giữa = item trên + 1)
                onItemSelected(snapIndex + 1)
            }
        }
    }
}

/**
 * Phần chọn thời gian sử dụng 2 LazyColumn.
 * 🚨 ĐÃ SỬA LỖI LAYOUT VÀ LOGIC
 */
@Composable
fun TimePickerSection(
    hourListState: LazyListState,
    minuteListState: LazyListState,
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    // Áp dụng logic snapping
    rememberSnapLogic(lazyListState = hourListState, onItemSelected = onHourChange)
    rememberSnapLogic(lazyListState = minuteListState, onItemSelected = onMinuteChange)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Cố định chiều cao (ví dụ: 3 item x 60dp)
            .padding(vertical = 20.dp),
        // Kéo 3 thành phần lại gần nhau
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Cột Giờ ---
        LazyColumn(
            modifier = Modifier.wrapContentWidth(), // Bọc nội dung
            state = hourListState,
            horizontalAlignment = Alignment.End, // Căn phải
            // Padding dọc = (Cao 180 / 2) - (Cao item ~50 / 2) ≈ 65.dp
            contentPadding = PaddingValues(vertical = 0.dp)
        ) {
            items(24) { hour ->
                TimePickerItem(
                    text = "%02d".format(hour),
                    isSelected = (hour == selectedHour)
                )
            }
        }

        // --- Dấu : ---
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 12.dp) // Tinh chỉnh chiều dọc
        )

        // --- Cột Phút ---
        LazyColumn(
            modifier = Modifier.wrapContentWidth(), // Bọc nội dung
            state = minuteListState,
            horizontalAlignment = Alignment.Start, // Căn trái
            contentPadding = PaddingValues(vertical = 0.dp)
        ) {
            items(60) { minute ->
                TimePickerItem(
                    text = "%02d".format(minute),
                    isSelected = (minute == selectedMinute)
                )
            }
        }
    }
}

/**
 * Helper Composable cho một Text (Giờ/Phút) trong TimePicker
 */
@Composable
private fun TimePickerItem(
    text: String,
    isSelected: Boolean
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        // Thay đổi style dựa trên state
        fontSize = if (isSelected) 36.sp else 32.sp,
        color = if (isSelected) Color.White else Color.Gray,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        // Bỏ hoàn toàn padding ngang
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

/**
 * Phần chọn ngày trong tuần
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
                .padding(0.dp,20.dp,0.dp,0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Hàng ngày",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
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
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) selectedColor else unselectedColor
            )
            .clickable { onClick() }
            .sizeIn(minWidth = 48.dp, minHeight = 40.dp)
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) onSelectedColor else onUnselectedColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Phần "Nhiệm vụ báo thức"
 */
@Composable
fun AlarmTaskSection() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Task 1: (Ví dụ)
        item {
            Box(modifier = Modifier){
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    modifier = Modifier
                        .size(80.dp)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp, start = 20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ){
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text("5 lần", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .align (Alignment.TopEnd)
                        .clickable(onClick = {}),
                    contentAlignment = Alignment.Center
                ){
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        // Các task bị khóa (Ví dụ)
        items(3) {
            Box(modifier = Modifier){
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.Gray),
                    modifier = Modifier
                        .size(80.dp)
                        .padding(top = 4.dp, end = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 4.dp, start = 20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ){
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black)
                        }
                    }
                }
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
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = Color.White
        )
        Text(
            text = "TOKUSOU SENTAI DEKAR...",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = Color.White
        )
        Icon(
            Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = null,
            tint = Color.White
        )
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
        Icon(
            Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = "Âm lượng",
            tint = Color.White
        )
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
        color = Color.White,
        fontSize = 18.sp,
        modifier = Modifier.fillMaxWidth()
    )
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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color =  Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Icon(
                Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        AlarmSettingScreen()
    }
}