package com.jovan.descripix.domain.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.remote.request.CaptionRequest
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.domain.model.Language

import com.jovan.descripix.domain.repository.IDescribitRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject

class DescripixInteractor @Inject constructor(private val repository: IDescribitRepository) :
    DescripixUseCase {

    override fun getSession(context: Context, isConnected: Boolean): Flow<SessionData> {
        Log.d("DescripixInteractor", "getSession called")
        return repository.getSession(context, isConnected)
    }

    override suspend fun login(
        googleId: String,
        context: Context
    ): ApiResponse<LoginResponse> = repository.login(googleId, context)

    override suspend fun saveLanguage(language: Language) {
        return repository.saveLanguage(language)
    }

    override fun getLanguage(): Flow<Language> {
        return repository.getLanguage()
    }

    override fun isConnected(): Flow<Boolean> = repository.isConnected()
    override suspend fun getAllCaptions(isConnected: Boolean, token: String, context: Context): Flow<List<CaptionEntity>> =
        repository.getAllCaptions(isConnected, token, context)

    override suspend fun getUserDetail(isConnected: Boolean, refreshToken: String, token: String, context: Context): Flow<UserEntity> =
        repository.getUserDetail(isConnected, refreshToken ,token, context)

    override suspend fun logout(refresh: String, context: Context): ApiResponse<Unit> {
        return repository.logout(refresh, context)
    }
    override suspend fun saveCaption(captionRequest: CaptionRequest, token: String, context: Context) : ApiResponse<CaptionDataResponse> =
        repository.saveCaption(captionRequest, token, context)

    override suspend fun deleteCaption(id: Int, token: String, context: Context): ApiResponse<Unit> =
        repository.deleteCaption(id, token, context)

    override suspend fun editCaption(id: Int, captionRequest: CaptionRequest, token: String, context: Context): ApiResponse<Unit> =
        repository.editCaption(id, captionRequest, token, context)

    override suspend fun generateCaption(metadata: JSONObject, image: Uri, context: Context) =
        repository.generateCaption(metadata, image, context)

    override suspend fun getCaptionDetails(id: Int, token: String, context: Context) : ApiResponse<CaptionDataResponse> =
        repository.getCaptionDetails(id, token, context)

    override suspend fun updateUserDetail(userRequest: UserRequest, token: String, context: Context): ApiResponse<Unit> =
        repository.updateUserDetail(userRequest, token, context)
}