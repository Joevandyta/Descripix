package com.jovan.descripix.data.source.remote.network

import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.GenerateResponse
import com.jovan.descripix.data.source.remote.response.ListCaptionResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.data.source.remote.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {

    //Authentication Start
    @FormUrlEncoded
    @POST("auth/google-login/")
    suspend fun googleLogin(
        @Field("googleId") googleId: String,
    ): ApiResponse<LoginResponse>

    //Logout
    @FormUrlEncoded
    @POST("auth/logout/")
    suspend fun logout(
        @Field("refresh") refresh: String
    ): ApiResponse<Unit>

    //Token refresh
    @FormUrlEncoded
    @POST("auth/token-refresh/")
    suspend fun refreshToken(
        @Field("refresh") refresh: String
    ): ApiResponse<LoginResponse>

    //Token Verify
    @GET("auth/token-verify/")
    suspend fun verifyToken(
        @Header("Authorization") token: String,
    ): ApiResponse<Unit>

    //User Detail
    @GET("auth/user-detail/")
    suspend fun getUserDetail(
        @Header("Authorization") token: String
    ): ApiResponse<UserResponse>

    @FormUrlEncoded
    @PUT("auth/user-edit/")
    suspend fun updateUserDetail(
        @Field("gender") gender: String?,
        @Field("birth_date") birthDate: String?,
        @Field("about_me") aboutMe: String?,
        @Header("Authorization") token: String
    ): ApiResponse<Unit>

    @Multipart
    @POST("caption/save/")
    suspend fun saveCaption(
        @Part("caption") caption: RequestBody?,
        @Part("author") author: RequestBody?,
        @Part("date") date: RequestBody?,
        @Part("location") location: RequestBody?,
        @Part("device") device: RequestBody?,
        @Part("model") model: RequestBody?,
        @Part image: MultipartBody.Part,
        @Header("Authorization") token: String
    ): ApiResponse<CaptionDataResponse>

    //Edit Caption
    @FormUrlEncoded
    @PUT("caption/detail/")
    suspend fun editCaption(
        @Query("id") id: Int,
        @Field("caption") caption: String?,
        @Field("author") author: String?,
        @Field("date") date: String?,
        @Field("location") location: String?,
        @Field("device") device: String?,
        @Field("model") model: String?,
        @Header("Authorization") token: String
    ): ApiResponse<Unit>

    @DELETE("caption/detail/")
    suspend fun deleteCaption(
        @Query("id") id: Int,
        @Header("Authorization") token: String
    ): ApiResponse<Unit>

    //Caption Details
    @GET("caption/detail/")
    suspend fun getCaptionDetails(
        @Query("id") id: Int,
        @Header("Authorization") token: String
    ): ApiResponse<CaptionDataResponse>

    //Generate Caption
    @Multipart
    @POST("caption/generate/")
    suspend fun generateCaption(
        @Part("metadata") metadata: RequestBody,
        @Part image: MultipartBody.Part,
    ): ApiResponse<GenerateResponse>

    //Caption List
    @GET("caption/list/")
    suspend fun getCaptionList(
        @Header("Authorization") token: String
    ): ApiResponse<List<ListCaptionResponse>>
}