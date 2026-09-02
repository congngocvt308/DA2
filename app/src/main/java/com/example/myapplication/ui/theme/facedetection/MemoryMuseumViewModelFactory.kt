package com.example.myapplication.ui.theme.facedetection


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MemoryMuseumViewModelFactory(
    private val repository: PictureRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryMuseumViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MemoryMuseumViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}