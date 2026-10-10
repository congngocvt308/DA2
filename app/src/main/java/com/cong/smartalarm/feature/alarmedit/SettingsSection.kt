package com.cong.smartalarm.feature.alarmedit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cong.smartalarm.core.designsystem.component.SoftSurface
import com.cong.smartalarm.core.designsystem.theme.Calm


/** Thẻ nổi mềm bo 28dp cho từng nhóm cài đặt. */
@Composable
internal fun SettingsSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    SoftSurface(
        cornerRadius = 28.dp,
        contentAlignment = Alignment.TopStart,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), content = content)
    }
}

/** Hàng bấm được bên trong thẻ cài đặt (nền xám rất nhạt). */
@Composable
internal fun SettingsRow(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Calm.NeutralMist)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Tiêu đề in đậm của từng thẻ cài đặt. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsSectionHeader(
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Calm.Foreground,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

/** Ô nhập tên báo thức trên kính mềm. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmLabelField(
    label: String,
    onLabelChange: (String) -> Unit
) {
    SoftSurface(
        cornerRadius = 22.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = label,
            onValueChange = { onLabelChange(it) },
            label = { Text("Tên báo thức", color = Calm.MutedForeground) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Calm.Foreground,
                unfocusedTextColor = Calm.Foreground,
                cursorColor = Calm.Primary
            ),
            leadingIcon = {
                Image(
                    painter = painterResource(com.cong.smartalarm.R.drawable.sun),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Sửa tên",
                    tint = Calm.MutedForeground
                )
            },
        )
    }
}
