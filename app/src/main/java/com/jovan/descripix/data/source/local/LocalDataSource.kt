package com.jovan.descripix.data.source.local

import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.local.room.CaptionDao
import com.jovan.descripix.data.source.local.room.UserDao
import com.jovan.descripix.domain.model.Language
import com.jovan.descripix.domain.repository.ILocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val userPreference: UserPreference,
    private val describitDao: CaptionDao,
    private val userDao: UserDao
) : ILocalDataSource {
    override suspend fun saveSession(user: SessionData) {
        userPreference.saveSession(user)
    }

    override fun getSession(): Flow<SessionData> {
        return userPreference.getSession().distinctUntilChanged()
    }

    override suspend fun logout() {
        userPreference.logout()
    }

    override suspend fun saveLanguage(language: Language) {
        return userPreference.saveLanguage(language)
    }

    override fun getLanguage(): Flow<Language> {
        return userPreference.getLanguage().distinctUntilChanged()
    }

    override suspend fun insertCaption(captionEntity: List<CaptionEntity>) {
        describitDao.insert(captionEntity)
    }

    override suspend fun deleteAllCaption() {
        describitDao.deleteAll()
    }

    override fun getAllCaption(): Flow<List<CaptionEntity>> {
        return describitDao.getAllCaption()
    }

    //User
    override suspend fun insertUser(userEntity: UserEntity) {
        userDao.insertUser(userEntity)
    }
    override suspend fun deleteUser() {
        userDao.deleteUser()
    }
    override fun getUser(id: String): Flow<UserEntity?> {
        return userDao.getUser(id)
    }

}