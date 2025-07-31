package com.jovan.descripix.data.source.remote

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
import com.jovan.descripix.domain.repository.IRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject
//
//class FakeRemoteDataSource: IRemoteDataSource {
//
////    var shouldReturnError = false
////    var isNetworkConnected = true
////    var errorMessage = "API Response Error"
////
////    private val mockLoginResponse = LoginResponse(
////        refresh = "fake_refresh_token",
////        access = "fake_access_token"
////    )
////
////    private val mockUserResponse = UserResponse(
////        id = "1",
////        username = "Test User",
////        email = "test@example.com",
////        gender = "Male",
////        birthDate = "1990-01-01",
////        aboutMe = "Test about me",
////        profileImg = "https://png.pngtree.com/png-vector/20191121/ourmid/pngtree-blue-bird-vector-or-color-illustration-png-image_2013004.jpg"
////    )
////    private val mockCaptionDataResponse = CaptionDataResponse(
////        id = 1,
////        caption = "Test caption",
////        author = "Test author",
////        date = "2023-12-01",
////        location = "Test location",
////        device = "Test device",
////        model = "Test model",
////        image = "https://png.pngtree.com/png-vector/20191121/ourmid/pngtree-blue-bird-vector-or-color-illustration-png-image_2013004.jpg",
////        uid = ""
////    )
////
////    private val mockGenerateResponse = GenerateResponse(
////        caption = "Generated caption Success",
////    )
////
////    private val mockCaptionList = listOf(
////        ListCaptionResponse(id = 1, caption = "Caption 1", image = "url1"),
////        ListCaptionResponse(id = 2, caption = "Caption 2", image = "url2"),
////        ListCaptionResponse(id = 3, caption = "Caption 3", image = "url3"),
////        ListCaptionResponse(id = 4, caption = "Caption 4", image = "url4"),
////        ListCaptionResponse(id = 5, caption = "Caption 5", image = "url5"),
////    )
////
////
////    override suspend fun googleLogin(googleId: String): ApiResponse<LoginResponse> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else {
////            ApiResponse(true, "Login successful", mockLoginResponse)
////        }
////    }
////
////    override fun isConnected(): Flow<Boolean> {
////        return flowOf(isNetworkConnected)
////    }
////
////    override suspend fun logout(refresh: String, context: Context): ApiResponse<Unit> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else {
////            ApiResponse(true, "Logout successful", Unit)
////        }
////    }
////
////    override suspend fun refreshToken(refresh: String): ApiResponse<LoginResponse> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else {
////            ApiResponse(true, "Token refreshed", mockLoginResponse.copy(
////                access = "new_fake_access_token"
////            ))
////        }
////    }
////
////    override suspend fun tokenVerify(token: String): ApiResponse<Unit> {
////        return if (shouldReturnError || token == "invalid_token") {
////            ApiResponse(false, "Invalid token", null)
////        } else {
////            ApiResponse(true, "Token verified", Unit)
////        }
////    }
////
////    override suspend fun getUserDetail(token: String, context: Context): ApiResponse<UserResponse> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else {
////            ApiResponse(true, "User details retrieved", mockUserResponse)
////        }
////    }
////
////    override suspend fun updateUserDetail(
////        userRequest: UserRequest,
////        token: String,
////        context: Context
////    ): ApiResponse<Unit> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else {
////            ApiResponse(true, "User details updated", Unit)
////        }
////    }
////
////    override suspend fun saveCaption(
////        captionRequest: CaptionRequest,
////        token: String,
////        context: Context
////    ): ApiResponse<CaptionDataResponse> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else if (captionRequest.image == "invalid_uri") {
////            ApiResponse(false, "Failed to load image", null)
////        } else {
////            ApiResponse(true, "Caption saved", mockCaptionDataResponse.copy(
////                caption = captionRequest.caption,
////                author = captionRequest.author,
////                date = captionRequest.date,
////                location = captionRequest.location,
////                device = captionRequest.device,
////                model = captionRequest.model
////            ))
////        }
////    }
////
////    override suspend fun deleteCaption(
////        id: Int,
////        token: String,
////        context: Context
////    ): ApiResponse<Unit> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else if (id == -1) {
////            ApiResponse(false, "Caption not found", null)
////        } else {
////            ApiResponse(true, "Caption deleted", Unit)
////        }
////
////    }
////
////    override suspend fun editCaption(
////        id: Int,
////        captionRequest: CaptionRequest,
////        token: String,
////        context: Context
////    ): ApiResponse<Unit> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else if (id == -1) {
////            ApiResponse(false, "Caption not found", null)
////        } else {
////            ApiResponse(true, "Caption updated", Unit)
////        }
////    }
////
////    override suspend fun getCaptionDetails(
////        id: Int,
////        token: String,
////        context: Context
////    ): ApiResponse<CaptionDataResponse> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else if (id == -1) {
////            ApiResponse(false, "Caption not found", null)
////        } else {
////            ApiResponse(true, "Caption details retrieved", mockCaptionDataResponse.copy(id = id))
////        }
////    }
////
////    override suspend fun generateCaption(
////        metadata: JSONObject,
////        image: Uri,
////        context: Context
////    ): ApiResponse<GenerateResponse> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else if (image.toString() == "invalid_uri") {
////            ApiResponse(false, "Failed to load image", null)
////        } else {
////            ApiResponse(true, "Caption generated", mockGenerateResponse)
////        }
////    }
////
////
////    override suspend fun getCaptionList(
////        token: String,
////        context: Context
////    ): ApiResponse<List<ListCaptionResponse>> {
////        return if (shouldReturnError) {
////            ApiResponse(false, errorMessage, null)
////        } else {
////            ApiResponse(true, "Caption list retrieved", mockCaptionList)
////        }
////    }
////
////    fun setNetworkError(shouldError: Boolean, message: String = "Network error") {
////        shouldReturnError = shouldError
////        errorMessage = message
////    }
////
////    fun setNetworkConnected(connected: Boolean) {
////        isNetworkConnected = connected
////    }
////
////    fun reset() {
////        shouldReturnError = false
////        isNetworkConnected = true
////        errorMessage = "API Response Error"
////    }
//}

