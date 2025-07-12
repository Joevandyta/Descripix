package com.jovan.descripix.domain.usecase

import android.content.Context
import android.net.Uri
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.remote.request.CaptionRequest
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.GenerateResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.domain.model.Language
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface DescripixUseCase {


    fun getSession(context: Context, isConnected: Boolean): Flow<SessionData>

    suspend fun logout(refresh: String, context: Context): ApiResponse<Unit>

    suspend fun login(googleId: String, context: Context): ApiResponse<LoginResponse>

    suspend fun saveLanguage(language: Language)

    fun getLanguage(): Flow<Language>

    fun isConnected(): Flow<Boolean>

    suspend fun getAllCaptions(isConnected: Boolean, token: String, context: Context): Flow<List<CaptionEntity>>

    suspend fun getUserDetail(isConnected: Boolean, refreshToken: String, token: String, context: Context): Flow<UserEntity>

    suspend fun saveCaption(captionRequest: CaptionRequest, token: String, context: Context) : ApiResponse<CaptionDataResponse>

    suspend fun deleteCaption(id: Int, token: String, context: Context) : ApiResponse<Unit>

    suspend fun editCaption(id: Int, captionRequest: CaptionRequest, token: String, context: Context): ApiResponse<Unit>

    suspend fun generateCaption(metadata: JSONObject, image: Uri, context: Context) : ApiResponse<GenerateResponse>

    suspend fun getCaptionDetails(id: Int, token: String, context: Context) : ApiResponse<CaptionDataResponse>

    suspend fun updateUserDetail(userRequest: UserRequest, token: String, context: Context): ApiResponse<Unit>
}