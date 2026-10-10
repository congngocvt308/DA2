package com.cong.smartalarm.data.repository

import com.cong.smartalarm.core.database.dao.AppDao
import com.cong.smartalarm.core.database.entity.PictureEntity
import kotlinx.coroutines.flow.Flow

/** Ảnh nụ cười trong Bảo tàng nụ cười. */
class PictureRepository(private val appDao: AppDao) {
    fun getAllPicturesByUser(userId: Int = 1): Flow<List<PictureEntity>> {
        return appDao.getAllPicturesByUser(userId)
    }

    suspend fun insertPicture(picture: PictureEntity): Long = appDao.insertPicture(picture)

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