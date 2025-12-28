package com.jovan.descripix.data

import android.content.Context
import android.net.Uri
import com.jovan.descripix.R
import com.jovan.descripix.data.source.local.LocalDataSource
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.remote.RemoteDataSource
import com.jovan.descripix.data.source.remote.request.CaptionRequest
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.GenerateResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.domain.repository.IDescripixRepository
import com.jovan.descripix.utils.ImageConverter
import com.jovan.descripix.utils.handleApiException
import com.jovan.descripix.utils.reduceFileSize
import com.jovan.descripix.utils.resizeIfTooLarge
import com.jovan.descripix.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DescripixRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    ) : IDescripixRepository {


    //LocalDataSource
    override fun getSession(context: Context, isConnected: Boolean): Flow<SessionData> =
        localDataSource.getSession().map { session ->
            wrapEspressoIdlingResource {
                var currentSession = session
                if (currentSession.token.isBlank() || currentSession.refreshToken.isBlank()) {
                    localDataSource.logout()
                    return@map currentSession
                }
                if (isConnected) {
                    try {
                        val tokenVerify =
                            handleApiException { remoteDataSource.tokenVerify(currentSession.token) }
                        if (!tokenVerify.status && !tokenVerify.message.toString()
                                .contains(context.getString(R.string.connection_timeout))
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
                    } catch (_: Exception) {
                    }
                }
                currentSession
            }
        }.catch {
            emit(SessionData.empty())
        }


    override suspend fun logout(
        refresh: String,
        context: Context
    ): ApiResponse<Unit> {
        wrapEspressoIdlingResource {
            val response = remoteDataSource.logout(refresh, context)
            localDataSource.deleteUser()
            localDataSource.deleteAllCaption()
            localDataSource.logout()
            return response
        }
    }

    override suspend fun login(
        googleId: String,
        context: Context
    ): ApiResponse<LoginResponse> {
        wrapEspressoIdlingResource {
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
    }

    override fun isConnected(): Flow<Boolean> {
        wrapEspressoIdlingResource {
            return remoteDataSource.isConnected()
        }
    }

    override suspend fun getAllCaptions(
        isConnected: Boolean,
        token: String,
        context: Context
    ): Flow<List<CaptionEntity>> = flow {
        wrapEspressoIdlingResource {
            var currentCaption = localDataSource.getAllCaption().first()
            if (isConnected) {
                try {
                    val apiResponse = remoteDataSource.getCaptionList(token, context)
                    if (apiResponse.status) {
                        val captionsFromServer = apiResponse.data
                        try {
                            val entities = mutableListOf<CaptionEntity>()
                            captionsFromServer?.forEach { serverCaption ->
                                try {
                                    val imageFile = withContext(Dispatchers.IO) {
                                        ImageConverter.downloadImageToFile(
                                            context,
                                            serverCaption.image
                                        )
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
                                    }
                                } catch (_: Exception) {
                                }
                            }
                            currentCaption = entities

                        } catch (_: Exception) {
                            // Keep currentCaption as is (local data)
                        }
                    }
                } catch (_: Exception) {
                }
                val localCaptionBeforeProcessing = localDataSource.getAllCaption().first()
                if (currentCaption != localCaptionBeforeProcessing) {
                    try {
                        localDataSource.deleteAllCaption()
                        localDataSource.insertCaption(currentCaption)
                        emit(localDataSource.getAllCaption().first())
                    } catch (_: Exception) {
                        emit(currentCaption)
                    }
                } else {
                    emit(currentCaption)
                }
            } else {
                emit(currentCaption)
            }
        }
    }.distinctUntilChanged()

    override suspend fun getUserDetail(
        isConnected: Boolean,
        refreshToken: String,
        token: String,
        context: Context
    ): Flow<UserEntity> = flow {
        wrapEspressoIdlingResource {
            val localUser = localDataSource.getUser(refreshToken).first()
            if (isConnected) {
                val response = remoteDataSource.getUserDetail(token, context)
                val onlineUser = response.data

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
                emit(newLocalUser)
            }
        }
    }

    override suspend fun updateUserDetail(
        userRequest: UserRequest,
        token: String,
        context: Context
    ): ApiResponse<Unit> {
        wrapEspressoIdlingResource {
            return remoteDataSource.updateUserDetail(userRequest, token, context)
        }
    }

    //Remote Caption
    override suspend fun saveCaption(
        captionRequest: CaptionRequest,
        token: String,
        context: Context
    ): ApiResponse<CaptionDataResponse> {
        wrapEspressoIdlingResource {
            return remoteDataSource.saveCaption(captionRequest, token, context)
        }
    }

    override suspend fun deleteCaption(
        id: Int,
        token: String,
        context: Context
    ): ApiResponse<Unit> {
        wrapEspressoIdlingResource {
            return remoteDataSource.deleteCaption(id, token, context)
        }
    }

    override suspend fun editCaption(
        id: Int,
        captionRequest: CaptionRequest,
        token: String,
        context: Context
    ): ApiResponse<Unit> {
        wrapEspressoIdlingResource {
            return remoteDataSource.editCaption(id, captionRequest, token, context)
        }
    }

    override suspend fun generateCaption(
        token: String,
        languageCode: String,
        metadata: JSONObject,
        image: Uri,
        style: String,
        context: Context
    ): ApiResponse<GenerateResponse> {
        wrapEspressoIdlingResource {
            return remoteDataSource.generateCaption(token, languageCode, metadata, image, style, context)
        }
    }

    override suspend fun getCaptionDetails(
        id: Int,
        token: String,
        context: Context
    ): ApiResponse<CaptionDataResponse> {
        wrapEspressoIdlingResource {
            val response = remoteDataSource.getCaptionDetails(id, token, context)
            var finalResponse = response
            if (response.status && response.data?.date == null) {
                finalResponse = response.copy(data = response.data?.copy(date = ""))
            }
            return finalResponse
        }
    }
}