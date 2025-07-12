package com.jovan.descripix.domain.repository

import androidx.datastore.preferences.core.edit
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.domain.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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