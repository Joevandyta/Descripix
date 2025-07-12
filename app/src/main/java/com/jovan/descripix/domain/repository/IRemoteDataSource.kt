package com.jovan.descripix.domain.repository

import android.content.Context
import android.net.Uri
import com.jovan.descripix.data.source.remote.request.CaptionRequest
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.GenerateResponse
import com.jovan.descripix.data.source.remote.response.ListCaptionResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.data.source.remote.response.UserResponse
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface IRemoteDataSource {


    suspend fun googleLogin(
        googleId: String,
    ): ApiResponse<LoginResponse>

    fun isConnected(): Flow<Boolean>

    suspend fun logout(refresh: String, context: Context): ApiResponse<Unit>

    suspend fun refreshToken(refresh: String): ApiResponse<LoginResponse>


    suspend fun tokenVerify(token: String): ApiResponse<Unit>

    suspend fun getUserDetail(token: String, context: Context): ApiResponse<UserResponse>

    suspend fun updateUserDetail(userRequest: UserRequest, token: String, context: Context): ApiResponse<Unit>
    suspend fun saveCaption(captionRequest: CaptionRequest, token: String, context: Context): ApiResponse<CaptionDataResponse>

    suspend fun deleteCaption(id: Int, token: String, context: Context): ApiResponse<Unit>

    suspend fun editCaption(
        id: Int,
        captionRequest: CaptionRequest,
        token: String,
        context: Context
    ): ApiResponse<Unit>

    suspend fun getCaptionDetails(
        id: Int,
        token: String,
        context: Context
    ): ApiResponse<CaptionDataResponse>

    suspend fun generateCaption(
        metadata: JSONObject,
        image: Uri,
        context: Context
    ): ApiResponse<GenerateResponse>

    suspend fun getCaptionList(
        token: String,
        context: Context
    ): ApiResponse<List<ListCaptionResponse>>


}