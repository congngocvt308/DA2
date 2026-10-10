package com.cong.smartalarm.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cong.smartalarm.core.designsystem.component.SoftSurface
import com.cong.smartalarm.core.designsystem.theme.Calm
import com.cong.smartalarm.core.designsystem.theme.rememberReducedMotion

/** Thanh điều hướng nổi 3 tab (Báo thức / Chủ đề / Thống kê) với viên chỉ báo trượt. */
@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf("Báo thức", "Chủ đề", "Thống kê")
    val icons = listOf(Icons.Filled.Alarm, Icons.Filled.Style, Icons.Filled.Assessment)
    val routes = listOf(Screen.ALARM_TAB, Screen.TOPIC_TAB, Screen.STATS_TAB)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedIndex = routes.indexOf(currentRoute).coerceAtLeast(0)
    val navWidth = 320.dp
    val itemWidth = navWidth / items.size

    val reducedMotion = rememberReducedMotion()
    val indicatorOffset by animateDpAsState(
        targetValue = itemWidth * selectedIndex,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 250),
        label = "indicatorOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Thanh điều hướng nổi mềm (soft UI)
        SoftSurface(
            shape = RoundedCornerShape(40.dp),
            elevation = 12.dp,
            modifier = Modifier
                .width(navWidth)
                .height(64.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Vùng xám chỉ báo (Sliding Pill)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(itemWidth - 12.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Calm.Primary.copy(alpha = 0.16f)) // Viên chỉ báo tab đang chọn: hổ phách nhạt
                        .border(1.dp, Calm.Primary.copy(alpha = 0.28f), RoundedCornerShape(32.dp))
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .semantics { selected = isSelected; role = Role.Tab }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (currentRoute != routes[index]) {
                                        navController.navigate(routes[index]) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = null, // nhãn chữ bên dưới đã đọc tên tab
                                    modifier = Modifier.size(22.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Calm.MutedForeground
                                )
                                Text(
                                    text = item,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Calm.MutedForeground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
