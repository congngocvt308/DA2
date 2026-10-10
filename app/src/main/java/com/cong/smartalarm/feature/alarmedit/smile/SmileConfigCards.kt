package com.cong.smartalarm.feature.alarmedit.smile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cong.smartalarm.core.designsystem.component.CalmCard
import com.cong.smartalarm.core.designsystem.component.CalmSlider
import com.cong.smartalarm.core.designsystem.theme.Calm

/** Thẻ chỉnh thời gian giữ nụ cười (1–9 giây). */
@Composable
internal fun SmileDurationCard(
    smileDuration: Float,
    onDurationChange: (Float) -> Unit
) {
    CalmCard(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Calm.Border),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ){
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Thời gian cười: ${smileDuration.toInt()} giây")
            }
            CalmSlider(
                value = smileDuration,
                onValueChange = onDurationChange,
                valueRange = 1f..9f,
                steps = 12,
                modifier = Modifier.padding(top = 12.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1s", style = MaterialTheme.typography.bodySmall)
                Text("9s", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** Thẻ "Hình nền" với ảnh xem trước tỉ lệ điện thoại; bấm để đổi ảnh. */
@Composable
internal fun BackgroundPreviewCard(
    imageUri: Uri,
    onClick: () -> Unit
) {
    CalmCard(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Calm.Border),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ){
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hình nền:", modifier = Modifier.weight(1f))

            AsyncImage(
                model = imageUri,
                contentDescription = "Background Preview",
                modifier = Modifier
                    .width(52.dp)
                    .aspectRatio(9f / 19.5f) // Tỷ lệ màn hình điện thoại chuẩn
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )
        }
    }
}
