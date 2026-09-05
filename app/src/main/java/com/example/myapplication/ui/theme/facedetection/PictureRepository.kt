package com.cong.smartalarm.ui.theme.facedetection

import com.cong.smartalarm.data.AppDao
import com.cong.smartalarm.data.PictureEntity
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