package com.cong.smartalarm.ui.theme.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cong.smartalarm.R
import com.cong.smartalarm.data.PictureEntity

@Composable
fun FeaturedPolaroidCard(
    picture: PictureEntity,
    onCardClick: () -> Unit, // 🎯 Bấm vào bất kỳ đâu trên card cũng mở Dialog
    modifier: Modifier = Modifier
) {
    val displayName = picture.description?.ifBlank { "Kỷ niệm đẹp" } ?: "Kỷ niệm đẹp"

    Box(
        modifier = modifier
            .width(220.dp)
            .height(260.dp)
            .clickable { onCardClick() }, // 🚀 Click toàn bộ Card
        contentAlignment = Alignment.Center
    ) {
        // LAYER 1: Ảnh người dùng
        Box(
            modifier = Modifier
                .width(155.dp)
                .height(155.dp)
                .align(Alignment.TopCenter)
                .offset(y = 38.dp, x = (-5).dp)
                .graphicsLayer { rotationZ = -9.2f }
                .clip(RoundedCornerShape(4.dp))
        ) {
            AsyncImage(
                model = picture.imageUri,
                contentDescription = picture.description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // LAYER 2: Khung Polaroid
        Image(
            painter = painterResource(id = R.drawable.ic_polaroid_frame),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // LAYER 3: Text hiển thị tĩnh (Không cho sửa ở đây nữa)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
                .fillMaxWidth(0.7f)
                .graphicsLayer { rotationZ = -6f },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName,
                color = Color(0xFF2B2B2B),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}