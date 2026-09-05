package com.cong.smartalarm.data

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
