package com.cong.smartalarm.feature.museum

import com.cong.smartalarm.data.repository.PictureRepository
import com.cong.smartalarm.core.designsystem.component.LocalGlassBackdrop
import androidx.compose.runtime.CompositionLocalProvider

import com.cong.smartalarm.core.designsystem.component.GlassCard
import com.cong.smartalarm.core.designsystem.component.rememberReducedMotion
import com.cong.smartalarm.core.designsystem.theme.Calm
import androidx.compose.ui.graphics.Brush

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.cong.smartalarm.core.database.AppDatabase
import com.cong.smartalarm.core.database.entity.PictureEntity
import com.cong.smartalarm.feature.museum.component.CustomAlarmCard
import com.cong.smartalarm.feature.museum.component.FeaturedPolaroidCard
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
    // Màn có camera/ảnh phía sau: kính không khúc xạ nền aurora (sẽ lệch), dùng kính "khói".
    CompositionLocalProvider(LocalGlassBackdrop provides null) {
        MemoryMuseumContent(onBackClick, viewModel)
    }
}

@Composable
private fun MemoryMuseumContent(
    onBackClick: () -> Unit,
    viewModel: MemoryMuseumViewModel
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

    val reducedMotion = rememberReducedMotion()

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
        // 1. Background: tranh phòng trưng bày
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
                color = MaterialTheme.colorScheme.primary
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
                                    modifier = Modifier.matchParentSize(),
                                    enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.92f),
                                    exit = fadeOut(tween(400))
                                ) {
                                    TapGuideOverlay(reducedMotion = reducedMotion)
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
                .background(Calm.Surface, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Calm.Foreground
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
        GlassCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Calm.Surface),
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
                        .background(Calm.LavenderMist),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Calm.Primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Bảo Tàng Đang Trống ✨",
                    color = Calm.Foreground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hãy chụp ảnh lại khi thực hiện thử thách nụ cười để có những bức ảnh ký ức nhé!",
                    color = Calm.MutedForeground,
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

/**
 * Hướng dẫn "chạm vào ảnh" kiểu coach mark: gợn sóng lan ra từ điểm chạm, nút tay trắng nổi
 * nhấn nhẹ theo nhịp, và thẻ chú thích đen có mũi nhọn chỉ lên điểm chạm.
 * Giảm chuyển động: giữ một vòng gợn tĩnh, tay không nhấn.
 */
@Composable
private fun TapGuideOverlay(reducedMotion: Boolean) {
    val transition = rememberInfiniteTransition(label = "TapGuide")
    val cycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1800, easing = LinearEasing)),
        label = "TapCycle"
    )
    val t = if (reducedMotion) 0.45f else cycle
    // Nhấn trong 0..18% đầu nhịp rồi nhả, phần còn lại đứng yên cho dịu mắt
    val press = if (reducedMotion) 0f else when {
        t < 0.09f -> t / 0.09f
        t < 0.18f -> 1f - (t - 0.09f) / 0.09f
        else -> 0f
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .drawBehind {
                        val center = Offset(size.width / 2, size.height / 2)
                        val minR = 26.dp.toPx()
                        val maxR = size.minDimension / 2
                        // Hai vòng gợn lệch nửa nhịp
                        listOf(t, (t + 0.5f) % 1f).forEach { p ->
                            val r = minR + (maxR - minR) * p
                            val a = (1f - p) * 0.55f
                            drawCircle(Color.White.copy(alpha = a * 0.45f), r, center)
                            drawCircle(Color.White.copy(alpha = a), r, center, style = Stroke(2.dp.toPx()))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Calm.Surface,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer {
                            scaleX = 1f - 0.1f * press
                            scaleY = 1f - 0.1f * press
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Calm.Primary,
                            modifier = Modifier
                                .size(30.dp)
                                .graphicsLayer { translationY = 2.dp.toPx() * press }
                        )
                    }
                }
            }

            // Mũi nhọn chỉ lên nút tay
            Canvas(Modifier.size(width = 16.dp, height = 8.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path, Calm.Primary)
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Calm.Primary,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Chạm để mở kỷ niệm",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "Xem ảnh lớn & đặt tên cho nụ cười",
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

