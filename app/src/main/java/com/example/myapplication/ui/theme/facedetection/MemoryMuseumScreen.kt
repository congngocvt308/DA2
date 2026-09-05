package com.cong.smartalarm.ui.theme.facedetection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cong.smartalarm.R
import com.cong.smartalarm.data.AppDatabase
import com.cong.smartalarm.data.PictureEntity
import com.cong.smartalarm.ui.theme.components.CustomAlarmCard
import com.cong.smartalarm.ui.theme.components.FeaturedPolaroidCard
import kotlinx.coroutines.delay

@Composable
fun MemoryMuseumScreen(
    onBackClick: () -> Unit,
    viewModel: MemoryMuseumViewModel = viewModel(
        factory = MemoryMuseumViewModelFactory(
            PictureRepository(AppDatabase.getDatabase(LocalContext.current).appDao())
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    var showGuidanceOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading, uiState.pictures.isNotEmpty()) {
        if (!uiState.isLoading && uiState.pictures.isNotEmpty()) {
            showGuidanceOverlay = true
            delay(3000L) // Giữ hiển thị trong 2 giây
            showGuidanceOverlay = false // Tự động mờ dần và biến mất
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            showGuidanceOverlay = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "HandGuideAnimation")

    val handScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HandScale"
    )

    val handAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HandAlpha"
    )

    Box(modifier = Modifier
        .fillMaxSize()
        // 🚀 Bắt sự kiện chạm bất kỳ đâu trên màn hình để ẩn hướng dẫn ngay lập tức
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(pass = PointerEventPass.Initial)
                if (showGuidanceOverlay) {
                    showGuidanceOverlay = false
                }
            }
        }
    ) {
        // 1. Background
        Image(
            painter = painterResource(id = R.drawable.bg_gallery),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Loading State
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFF4081)
            )
        } else {
            if (uiState.pictures.isEmpty()) {
                EmptyMuseumCard()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 70.dp, bottom = 24.dp)
                ) {
                    // Title Image
                    item {
                        Image(
                            painter = painterResource(id = R.drawable.ic_museum_title),
                            contentDescription = "Bảo tàng nụ cười",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(90.dp)
                        )
                    }

                    // Featured Card (Ảnh đầu tiên)
                    uiState.featuredPicture?.let { featuredPic ->
                        item {
                            Box(contentAlignment = Alignment.Center) {
                                FeaturedPolaroidCard(
                                    picture = featuredPic,
                                    onCardClick = {
                                        showGuidanceOverlay = false
                                        viewModel.onPictureClick(featuredPic)
                                    } // 🚀 Bấm đâu trên Card cũng mở Dialog
                                )

                                AnimatedVisibility(
                                    visible = showGuidanceOverlay,
                                    enter = fadeIn(animationSpec = tween(300)),
                                    exit = fadeOut(animationSpec = tween(400))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(220.dp)
                                            .height(260.dp)
                                    ) {
                                        // 🗨️ BONG BÓNG CHAT HƯỚNG DẪN
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color.White,
                                            shadowElevation = 8.dp,
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .offset(y = (-35).dp)
                                        ) {
                                            Text(
                                                text = "Chạm vào ảnh để xem &\nsửa tên kỷ niệm nhé! ✨",
                                                color = Color(0xFF222222),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        // 👆 ICON BÀN TAY TRỎ VÀO ẢNH (NHẤP NHÁY + PHẬP PHỒNG ZOOM IN/OUT)
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = "Chạm vào đây",
                                            tint = Color(0xFFFFD1A4).copy(alpha = handAlpha),
                                            modifier = Modifier
                                                .size(54.dp)
                                                .align(Alignment.Center)
                                                .scale(handScale)
                                                .graphicsLayer { rotationZ = -15f }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    // Masonry Grid 3 Cột cho ảnh còn lại
                    if (uiState.column1Pictures.isNotEmpty() ||
                        uiState.column2Pictures.isNotEmpty() ||
                        uiState.column3Pictures.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Column 1
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    uiState.column1Pictures.forEach { item ->
                                        SmallCardPhotoItem(
                                            picture = item,
                                            onCardClick = { viewModel.onPictureClick(item) }
                                        )
                                    }
                                }

                                // Column 2
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    uiState.column2Pictures.forEach { item ->
                                        SmallCardPhotoItem(
                                            picture = item,
                                            onCardClick = { viewModel.onPictureClick(item) }
                                        )
                                    }
                                }

                                // Column 3
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    uiState.column3Pictures.forEach { item ->
                                        SmallCardPhotoItem(
                                            picture = item,
                                            onCardClick = { viewModel.onPictureClick(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
            // 3. Content List


        // 4. Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 16.dp)
                .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White
            )
        }

        // 5. Image Detail Dialog
        uiState.selectedPictureForDetail?.let { selectedPic ->
            ImageDetailDialog(
                picture = selectedPic,
                onDismiss = { viewModel.onDismissDetailDialog() },
                onDeletePhoto = { viewModel.onDeletePicture(it) },
                onUpdateDescription = { pic, newText ->
                    viewModel.onUpdateDescription(pic, newText) // 🚀 Gọi ViewModel cập nhật DB
                }
            )
        }
    }
}

@Composable
private fun EmptyMuseumCard() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E162B).copy(alpha = 0.88f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Máy ảnh Polaroid
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4081).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Bảo Tàng Đang Trống ✨",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hãy chụp ảnh lại khi thực hiện thử thách nụ cười để có những bức ảnh ký ức nhé!",
                    color = Color(0xFFD1C4E9),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SmallCardPhotoItem(
    picture: PictureEntity,
    onCardClick: () -> Unit
) {
    CustomAlarmCard(
        imageUri = picture.imageUri,
        title = picture.description ?: "",
        imageHeight = 85.dp,
        cornerRadius = 10.dp,
        onCardClick = onCardClick,
        modifier = Modifier.fillMaxWidth()
    )
}