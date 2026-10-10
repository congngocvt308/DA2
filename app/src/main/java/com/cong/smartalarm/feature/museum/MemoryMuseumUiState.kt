package com.cong.smartalarm.feature.museum


import com.cong.smartalarm.core.database.entity.PictureEntity

data class MemoryMuseumUiState(
    val isLoading: Boolean = true,
    val pictures: List<PictureEntity> = emptyList(),
    val featuredPicture: PictureEntity? = null,
    /** Các ảnh còn lại (sau ảnh nổi bật), hiển thị trong lưới so le 3 cột. */
    val otherPictures: List<PictureEntity> = emptyList(),
    val selectedPictureForDetail: PictureEntity? = null,
    val errorMessage: String? = null
)
