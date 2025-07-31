package com.jovan.descripix.domain.repository

import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.ui.common.Language
import kotlinx.coroutines.flow.Flow

interface ILocalDataSource {
    suspend fun saveSession(user: SessionData)

    fun getSession(): Flow<SessionData>

    suspend fun logout()

    suspend fun saveLanguage(language: Language)

    fun getLanguage(): Flow<Language>

    suspend fun insertCaption(captionEntity: List<CaptionEntity>)

    suspend fun deleteAllCaption()

    fun getAllCaption(): Flow<List<CaptionEntity>>

    suspend fun insertUser(userEntity: UserEntity)

    suspend fun deleteUser()

    fun getUser(id: String): Flow<UserEntity?>


}