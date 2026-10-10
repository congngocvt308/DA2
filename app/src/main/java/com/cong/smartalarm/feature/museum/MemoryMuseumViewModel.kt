package com.cong.smartalarm.feature.museum

import com.cong.smartalarm.data.repository.PictureRepository


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cong.smartalarm.feature.museum.MemoryMuseumUiState
import com.cong.smartalarm.core.database.entity.PictureEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MemoryMuseumViewModel(
    private val repository: PictureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryMuseumUiState())
    val uiState: StateFlow<MemoryMuseumUiState> = _uiState.asStateFlow()

    init {
        loadPictures()
    }

    private fun loadPictures(userId: Int = 1) {
        viewModelScope.launch {
            repository.getAllPicturesByUser(userId)
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message)
                    }
                }
                .collect { list ->
                    // Lưới so le tự xếp cột; ViewModel chỉ cần tách ảnh nổi bật
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pictures = list,
                            featuredPicture = list.firstOrNull(),
                            otherPictures = list.drop(1)
                        )
                    }
                }
        }
    }

    fun onPictureClick(picture: PictureEntity) {
        _uiState.update { it.copy(selectedPictureForDetail = picture) }
    }

    fun onDismissDetailDialog() {
        _uiState.update { it.copy(selectedPictureForDetail = null) }
    }

    fun onDeletePicture(picture: PictureEntity) {
        viewModelScope.launch {
            repository.deletePicture(picture)
            onDismissDetailDialog()
        }
    }

    fun onUpdateDescription(picture: PictureEntity, newDescription: String) {
        viewModelScope.launch {
            repository.updateDescription(picture, newDescription)
        }
    }
}