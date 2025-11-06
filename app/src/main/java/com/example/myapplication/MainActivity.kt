package com.example.myapplication
import AlarmCard
import SplashScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {

    var showSplashScreen by remember { mutableStateOf(true) }
    if (showSplashScreen) {
        SplashScreen(onTimeout = { showSplashScreen = false })
    } else {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    val alarms = remember {
        mutableStateListOf(
            Alarm(1, "Hàng ngày", "06:30", "Thức dậy", true),
            Alarm(2, "Cuối tuần", "08:00", "Đi chơi", false),
            Alarm(3, "Thứ 2, 4, 6", "14:00", "Học Flutter", true),
            Alarm(4, "Chủ nhật", "22:00", null, true)
        )
    }
    Scaffold(
        topBar = { TopBarContent() },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFFE50043),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    "Thêm báo thức",
                    tint = Color.White,
                        modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color.Black
    ){ paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = alarms,
                key = { index, alarm -> alarm.id })
            { index, alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { newCheckedState ->
                        alarms[index] = alarms[index].copy(isEnabled = newCheckedState)
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavBar() {
    val items = listOf("Báo thức", "Chủ đề", "Thống kê", "Cài đặt")
    val icons = listOf(
        Icons.Filled.Alarm,
        Icons.Filled.Style,
        Icons.Filled.Assessment,
        Icons.Filled.Settings
    )
    val selectedItem = remember { mutableStateOf(items[0]) }
    NavigationBar(
        containerColor = Color(0xFF1C1C1E),
        modifier = Modifier.height(70.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedItem.value == item
            NavigationBarItem(
                icon = {
                    Icon(
                        icons[index],
                        contentDescription = item,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { selectedItem.value = item },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFF8E8E93),
                    unselectedTextColor = Color(0xFF8E8E93),
                    indicatorColor = Color(0xFF1C1C1E)
                )
            )
        }
    }
}

@Composable
fun TopBarContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Đổ chuông sau 9 giờ 4 phút.",
            fontSize = 25.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold

        )
        Icon(
            Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}