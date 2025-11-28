package com.example.myapplication.ui.theme.alarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

// Màu sắc
val DarkBg = Color.Black
val CardSurface = Color(0xFF2C2C2E)
val RedPrimary = Color(0xFFE50043)
val BlueAccent = Color(0xFF00ACC1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(
    viewModel: AlarmSettingsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onMissionSettingClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
        }
    }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = Int.MAX_VALUE / 2)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = Int.MAX_VALUE / 2)

    LaunchedEffect(uiState.hour, uiState.minute) {
        hourListState.scrollToItem(max(0, uiState.hour - 1))
        minuteListState.scrollToItem(max(0, uiState.minute - 1))
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RedPrimary)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ){
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text("Chuông báo thức", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, "Đóng", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBg)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ){
                item {
                    OutlinedTextField(
                        value = uiState.label,
                        onValueChange = { viewModel.onLabelChanged(it) },
                        label = { Text("Tên báo thức", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedPrimary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = {
                            Image(
                                painter = painterResource(com.example.myapplication.R.drawable.sun),
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
                    )
                }

                // --- 1. PHẦN CHỈNH THỜI GIAN (2 LazyColumn) ---
                item {
                    TimePickerSection(
                        hourListState = hourListState,
                        minuteListState = minuteListState,
                        selectedHour = uiState.hour,
                        selectedMinute = uiState.minute,
                        onHourChange = { newHour -> viewModel.updateHour(newHour) },
                        onMinuteChange = { newMinute -> viewModel.updateMinute(newMinute) }
                    )
                }

                //--- 2. ĐỔ CHUÔNG SAU ---
                item {
                    Text(
                        text = uiState.timeUntilAlarm, // Có thể thêm logic tính toán
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }

                //--- 3. CHỌN NGÀY ---
                item {
                    DaySelectorSection(
                        daysOfWeek = uiState.daysOfWeek,
                        onRepeatDailyChange = { isChecked ->
                            viewModel.toggleRepeatDaily(isChecked)
                        },
                        onDayToggle = { day ->
                            viewModel.toggleDay(day)
                        }
                    )
                }

                // --- 4. NHIỆM VỤ BÁO THỨC ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
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
                            .padding(top = 20.dp),
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
                            VolumeSliderRow(
                                volume = uiState.volume,
                                onVolumeChange = viewModel::updateVolume
                            )
                        }
                    }
                }

                // --- 7. CÀI ĐẶT TÙY CHỈNH ---
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
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
        Button(
            onClick = { viewModel.saveAlarm() },
            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp)
                .height(56.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Lưu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

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

@Composable
fun DayCircleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) BlueAccent else Color(0xFF1C1C1E))
            .clickable { onClick() }
    ) {
        Text(text = text, color = if (isSelected) Color.White else BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RememberSnapLogic(
    lazyListState: LazyListState,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            coroutineScope.launch {
                delay(100)
                val firstVisibleItem = lazyListState.firstVisibleItemIndex
                val firstVisibleItemOffset = lazyListState.firstVisibleItemScrollOffset
                val itemHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 40

                val snapIndex = if (firstVisibleItemOffset > (itemHeight / 2)) {
                    firstVisibleItem + 1
                } else {
                    firstVisibleItem
                }
                lazyListState.animateScrollToItem(snapIndex)
                onItemSelected(snapIndex + 1)
            }
        }
    }
}

@Composable
fun MissionSquareCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color(0xFF006064) else Color(0xFF3E3E3E)
    val tintColor = if (isSelected) BlueAccent else Color.Gray

    Card(
        modifier = Modifier.size(80.dp).clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tintColor)
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = label, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AlarmTaskSection() {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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

@Composable
fun TimePickerSection(
    hourListState: LazyListState,
    minuteListState: LazyListState,
    selectedHour: Int,
    selectedMinute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    RememberSnapLogic(
        lazyListState = hourListState,
        onItemSelected = onHourChange
    )

    RememberSnapLogic(
        lazyListState = minuteListState,
        onItemSelected = onMinuteChange
    )

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
            modifier = Modifier.padding(horizontal = 12.dp) // Tinh chỉnh chiều dọc
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
@Composable
fun DaySelectorSection(
    daysOfWeek: Set<String>,
    onRepeatDailyChange: (Boolean) -> Unit,
    onDayToggle: (String) -> Unit
) {
    val isDaily = daysOfWeek.size == 7
    val selectedColor = MaterialTheme.colorScheme.primary
    val onSelectedColor = MaterialTheme.colorScheme.onPrimary
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant
    val onUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dayOrder = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRepeatDailyChange(!isDaily) }
                .padding(top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hàng ngày",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Checkbox(
                checked = isDaily,
                onCheckedChange = { onRepeatDailyChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = selectedColor,
                    uncheckedColor = Color.Gray,
                    checkmarkColor = onSelectedColor
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dayOrder.forEach { day ->
                val isSelected = daysOfWeek.contains(day)
                CustomDayChip(
                    text = day,
                    isSelected = isSelected,
                    selectedColor = selectedColor,
                    onSelectedColor = onSelectedColor,
                    unselectedColor = unselectedColor,
                    onUnselectedColor = onUnselectedColor,
                    onClick = { onDayToggle(day) }
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
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) selectedColor else unselectedColor)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) onSelectedColor else onUnselectedColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

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