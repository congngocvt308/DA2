package com.example.myapplication.ui.theme.facedetection

import com.example.myapplication.data.AppDao
import com.example.myapplication.data.PictureEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PictureRepository @Inject constructor(
    private val appDao: AppDao
) {
    fun getAllPicturesByUser(userId: Int = 1): Flow<List<PictureEntity>> {
        return appDao.getAllPicturesByUser(userId)
    }

    suspend fun savePicture(imageUri: String, alarmHistoryId: Int? = null, description: String? = null): Long {
        val picture = PictureEntity(
            userId = 1,
            alarmHistoryId = alarmHistoryId,
            imageUri = imageUri,
            description = description,
            createdAt = System.currentTimeMillis()
        )
        return appDao.insertPicture(picture)
    }

    suspend fun deletePicture(picture: PictureEntity) {
        appDao.deletePicture(picture)
    }

    suspend fun updateDescription(picture: PictureEntity, newDescription: String) {
        val updated = picture.copy(description = newDescription)
        appDao.updatePicture(updated)
    }
}