package com.jovan.descripix.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jovan.descripix.data.source.local.LocalDataSource
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.remote.RemoteDataSource
import com.jovan.descripix.data.source.remote.request.CaptionRequest
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.domain.model.Language
import com.jovan.descripix.domain.repository.IDescribitRepository
import com.jovan.descripix.utils.downloadImageToFile
import com.jovan.descripix.utils.handleApiException
import com.jovan.descripix.utils.reduceFileSize
import com.jovan.descripix.utils.resizeIfTooLarge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DescribitRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
) : IDescribitRepository {

    //LocalDataSource
    override fun getSession(context: Context, isConnected: Boolean): Flow<SessionData> =
        localDataSource.getSession().map { session ->
            var currentSession = session
            Log.d("REPO", "currentSession: $currentSession")
            if (currentSession.token.isBlank() || currentSession.refreshToken.isBlank()) {
                localDataSource.logout()
                return@map currentSession
            }
            if (isConnected) {
                try {
                    val tokenVerify =
                        handleApiException { remoteDataSource.tokenVerify(currentSession.token) }
                    if (!tokenVerify.status && !tokenVerify.message.toString()
                            .contains("Connection Timeout")
                    ) {
                        val refreshed =
                            handleApiException { remoteDataSource.refreshToken(currentSession.refreshToken) }
                        if (refreshed.status) {
                            currentSession = SessionData(
                                refreshToken = currentSession.refreshToken,
                                token = refreshed.data!!.access,
                                isLogin = true
                            )
                            localDataSource.saveSession(currentSession)
                        } else {
                            localDataSource.logout()
                            localDataSource.deleteUser()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("REPO", "Server error: ${e.message}")
                }
            }
            currentSession

        }.catch {
            emit(SessionData( refreshToken = "", token = "", isLogin = false))
        }


    override suspend fun logout(
        refresh: String,
        context: Context
    ): ApiResponse<Unit> {
        val response = remoteDataSource.logout(refresh, context)
        localDataSource.deleteUser()
        localDataSource.deleteAllCaption()
        localDataSource.logout()
        Log.d("REPO", "Logout Success")

        return response
    }

    override suspend fun saveLanguage(language: Language) {
        return localDataSource.saveLanguage(language)
    }

    override fun getLanguage(): Flow<Language> {
        return localDataSource.getLanguage()
    }

    override suspend fun login(
        googleId: String,
        context: Context
    ): ApiResponse<LoginResponse> {
        val response = remoteDataSource.googleLogin(googleId)
        if (response.status && response.data != null) {
            localDataSource.saveSession(
                SessionData(
                    refreshToken = response.data.refresh!!,
                    token = response.data.access,
                )
            )
        }
        return response
    }

    override fun isConnected(): Flow<Boolean> = remoteDataSource.isConnected()

    override suspend fun getAllCaptions(
        isConnected: Boolean,
        token: String,
        context: Context
    ): Flow<List<CaptionEntity>> = flow {
        var currentCaption = localDataSource.getAllCaption().first()
        Log.d("REPO-getAllCaptions", "${currentCaption.size}")
        if(isConnected){
            try {
                val apiResponse = remoteDataSource.getCaptionList(token, context)
                Log.d("REPO-apiResponse", "$apiResponse")

                if (apiResponse.status && !apiResponse.data.isNullOrEmpty()) {
                    val captionsFromServer = apiResponse.data

                    try {
                        val entities = mutableListOf<CaptionEntity>()

                        captionsFromServer.forEach { serverCaption ->
                            try {
                                val imageFile = withContext(Dispatchers.IO) {
                                    downloadImageToFile(context, serverCaption.image)
                                        ?.resizeIfTooLarge()
                                        ?.reduceFileSize()
                                }
                                if (imageFile != null) {
                                    entities.add(
                                        CaptionEntity(
                                            id = serverCaption.id,
                                            caption = serverCaption.caption,
                                            author = null,
                                            date = null,
                                            location = null,
                                            device = null,
                                            model = null,
                                            image = imageFile.absolutePath,
                                        )
                                    )
                                } else {
                                    Log.w("REPO", "Failed to download image for caption ${serverCaption.id}")
                                }
                            } catch (e: Exception) {
                                Log.e("REPO", "Error processing caption ${serverCaption.id}: ${e.message}")
                            }
                        }

                        if (entities.isNotEmpty()) {
                            currentCaption = entities
                        } else {
                            Log.w("REPO", "No valid entities processed from server data")
                        }

                    } catch (e: Exception) {
                        Log.e("REPO", "Error processing captions: ${e.message}")
                        // Keep currentCaption as is (local data)
                    }
                } else {
                    Log.d("REPO", "API response failed or empty data, keeping local data")
                }
            } catch (e: Exception) {
                Log.e("REPO", "Error fetching from server: ${e.message}")
                // Keep currentCaption as is (local data)
            }
            val localCaptionAfterProcessing = localDataSource.getAllCaption().first()
            if (currentCaption != localCaptionAfterProcessing) {
                try {
                    localDataSource.deleteAllCaption()
                    localDataSource.insertCaption(currentCaption)
                    emit(localDataSource.getAllCaption().first())
                    Log.d("REPO-updated", "Database updated with ${currentCaption.size} captions")
                } catch (e: Exception) {
                    Log.e("REPO", "Error updating database: ${e.message}")
                    // Emit current data even if database update fails
                    emit(currentCaption)
                }
            } else {
                Log.d("Caption is Same", "No changes detected, keeping existing data")
            }
        }else{
            emit(currentCaption)
        }
    }.distinctUntilChanged()

    override suspend fun getUserDetail(isConnected: Boolean, refreshToken: String, token: String,context: Context): Flow<UserEntity> = flow {
        val localUser = localDataSource.getUser(refreshToken).first()
        Log.d("Repository - getUserDetail", "localUser: $localUser")
        if (isConnected){
            val response = remoteDataSource.getUserDetail(token, context)
            val onlineUser = response.data
            Log.d("Repository - getUserDetail", "online user = $onlineUser")

            if (response.status && onlineUser != null) {

                if (localUser != null) localDataSource.deleteUser()
                val userEntity = UserEntity(
                    id = refreshToken,
                    username = response.data.username,
                    email = response.data.email,
                    gender = response.data.gender,
                    birthDate = response.data.birthDate,
                    aboutMe = response.data.aboutMe,
                    profileImg = response.data.profileImg,
                )
                localDataSource.insertUser(
                    userEntity
                )
                if (localUser != userEntity)
                    emit(userEntity)
            }
        }
        val newLocalUser = localDataSource.getUser(refreshToken).first()
        if (newLocalUser != null) {
            Log.d("REPO - getUserDetail", "TerEmit: $localUser")
            emit(newLocalUser)
        }
    }
    override suspend fun updateUserDetail(
        userRequest: UserRequest,
        token: String,
        context: Context
    ): ApiResponse<Unit> {
        Log.d("Repository - updateUserDetail", "userRequest: $userRequest")
        return remoteDataSource.updateUserDetail(userRequest, token, context)
    }

    //Remote Caption
    override suspend fun saveCaption(
        captionRequest: CaptionRequest,
        token: String,
        context: Context
    ): ApiResponse<CaptionDataResponse> =
        remoteDataSource.saveCaption(captionRequest, token, context)

    override suspend fun deleteCaption(
        id: Int,
        token: String,
        context: Context
    ): ApiResponse<Unit> =
        remoteDataSource.deleteCaption(id, token, context)

    override suspend fun editCaption(
        id: Int,
        captionRequest: CaptionRequest,
        token: String,
        context: Context
    ): ApiResponse<Unit> =
        remoteDataSource.editCaption(id, captionRequest, token, context)

    override suspend fun generateCaption(
        metadata: JSONObject,
        image: Uri,
        context: Context
    ) =
        remoteDataSource.generateCaption(metadata, image, context)

    override suspend fun getCaptionDetails(
        id: Int,
        token: String,
        context: Context
    ): ApiResponse<CaptionDataResponse> {
        val response = remoteDataSource.getCaptionDetails(id, token, context)
        var finalResponse = response
        if (response.status && response.data?.date == null) {
            finalResponse = response.copy(data = response.data?.copy(date = ""))
        }
        return finalResponse
    }





}