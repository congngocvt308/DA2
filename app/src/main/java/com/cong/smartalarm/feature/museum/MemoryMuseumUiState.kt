package com.cong.smartalarm.feature.museum

import com.cong.smartalarm.core.database.entity.PictureEntity

data class MemoryMuseumUiState(
    val isLoading: Boolean = true,
    val pictures: List<PictureEntity> = emptyList(),
    val featuredPicture: PictureEntity? = null,
    val column1Pictures: List<PictureEntity> = emptyList(),
    val column2Pictures: List<PictureEntity> = emptyList(),
    val column3Pictures: List<PictureEntity> = emptyList(),
    val selectedPictureForDetail: PictureEntity? = null,
    val errorMessage: String? = null
)
