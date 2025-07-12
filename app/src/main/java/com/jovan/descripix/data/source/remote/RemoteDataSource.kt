package com.jovan.descripix.data.source.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.jovan.descripix.data.source.remote.network.ApiService
import com.jovan.descripix.R
import com.jovan.descripix.data.source.remote.request.CaptionRequest
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.GenerateResponse
import com.jovan.descripix.data.source.remote.response.ListCaptionResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.data.source.remote.response.UserResponse
import com.jovan.descripix.domain.repository.IRemoteDataSource
import com.jovan.descripix.data.source.remote.network.ConnectivityObserver
import com.jovan.descripix.utils.downloadImageToFile
import com.jovan.descripix.utils.handleApiException
import com.jovan.descripix.utils.reduceFileSize
import com.jovan.descripix.utils.resizeIfTooLarge
import com.jovan.descripix.utils.uriToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val apiService: ApiService,
    private val connectivityObserver: ConnectivityObserver
) : IRemoteDataSource {

    private val verifyLock = Mutex()
    private var isTokenAlreadyVerified = false
    private var responseData = ApiResponse<Unit>(false, "", null)
    private var verifyResetJob: Job? = null

    override fun isConnected(): Flow<Boolean> = connectivityObserver.isConnected

    override suspend fun googleLogin(googleId: String): ApiResponse<LoginResponse> {
        return handleApiException {
            apiService.googleLogin(googleId)
        }
    }

    override suspend fun logout(refresh: String, context: Context): ApiResponse<Unit> {
        val response = handleApiException {
            apiService.logout(refresh)
        }
        if (response.status) isTokenAlreadyVerified = false
        return response
    }

    override suspend fun refreshToken(refresh: String): ApiResponse<LoginResponse> =
        handleApiException { apiService.refreshToken(refresh) }

    override suspend fun tokenVerify(token: String): ApiResponse<Unit> {
        verifyLock.withLock {
            if (!isTokenAlreadyVerified) {
                responseData = handleApiException { apiService.verifyToken(token = "Bearer $token") }

                if (responseData.status){
                    isTokenAlreadyVerified = true
                    verifyResetJob?.cancel()
                    verifyResetJob = CoroutineScope(Dispatchers.Default).launch {
                        delay(5_000) // 60 seconds
                        isTokenAlreadyVerified = false
                        Log.d("REPO", "Token verification expired 5 second.")
                    }
                } else {
                    verifyResetJob?.cancel()
                }
            }else{
                responseData
            }
        }
        return responseData
    }


    override suspend fun getUserDetail(token: String, context: Context): ApiResponse<UserResponse> {
        return handleApiException {
            apiService.getUserDetail(token = context.getString(R.string.bearer, token))
        }
    }

    override suspend fun updateUserDetail(
        userRequest: UserRequest,
        token: String,
        context: Context
    ): ApiResponse<Unit> {

        val gender =
            if (userRequest.gender.isNullOrBlank()) null else userRequest.gender
        val birthDate =
            if (userRequest.birthDate.isNullOrBlank()) null else userRequest.birthDate
        val aboutMe =
            if (userRequest.aboutMe.isNullOrBlank()) null else userRequest.aboutMe

        return handleApiException {
            apiService.updateUserDetail(gender, birthDate, aboutMe, token = context.getString(R.string.bearer, token))
        }
    }

    override suspend fun saveCaption(
        captionRequest: CaptionRequest,
        token: String,
        context: Context
    ): ApiResponse<CaptionDataResponse> {

        val caption = captionRequest.caption.toRequestBody()
        val author = captionRequest.author?.toRequestBody()
        val date = captionRequest.date?.toRequestBody()
        val location = captionRequest.location?.toRequestBody()
        val device = captionRequest.device?.toRequestBody()
        val model = captionRequest.model?.toRequestBody()
        val imageUri = captionRequest.image
        var multipartBody: MultipartBody.Part? = null
        try {
            val imageFile = withContext(Dispatchers.IO) {
                uriToFile(imageUri.toUri(), context).reduceFileSize().resizeIfTooLarge()
            }
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            multipartBody = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestImageFile
            )

        } catch (e: Exception) {
            val imageFile = withContext(Dispatchers.IO) {
                downloadImageToFile(context, imageUri)?.reduceFileSize()?.resizeIfTooLarge()
            }
            imageFile?.let {
                val requestImage = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                multipartBody = MultipartBody.Part.createFormData(
                    "image",
                    it.name,
                    requestImage
                )
            }

        }

        if (multipartBody == null) {
            return ApiResponse(false, context.getString(R.string.failed_to_load_image), null)
        }

        return handleApiException {
            apiService.saveCaption(
                caption,
                author,
                date,
                location,
                device,
                model,
                multipartBody!!,
                token = context.getString(R.string.bearer, token)
            )
        }
    }

    override suspend fun deleteCaption(
        id: Int,
        token: String,
        context: Context
    ): ApiResponse<Unit> =
        handleApiException { apiService.deleteCaption(id, token = context.getString(R.string.bearer, token)) }

    override suspend fun editCaption(
        id: Int,
        captionRequest: CaptionRequest,
        token: String,
        context: Context
    ): ApiResponse<Unit> {

        val caption = captionRequest.caption
        val author = captionRequest.author.toString()
        val date = captionRequest.date.toString()
        val location = captionRequest.location.toString()
        val device = captionRequest.device.toString()
        val model = captionRequest.model.toString()

        Log.d("RemoteDataSource", "Caption: $caption ")
        Log.d("RemoteDataSource", "Caption: $captionRequest ")

        return handleApiException {
            apiService.editCaption(
                id,
                caption,
                author,
                date,
                location,
                device,
                model,
                token = context.getString(R.string.bearer, token)
            )
        }
    }

    override suspend fun getCaptionDetails(
        id: Int,
        token: String,
        context: Context
    ): ApiResponse<CaptionDataResponse> =
        handleApiException { apiService.getCaptionDetails(id, token = context.getString(R.string.bearer, token)) }

    override suspend fun generateCaption(
        metadata: JSONObject,
        image: Uri,
        context: Context
    ): ApiResponse<GenerateResponse> {
        val metadataBody =
            metadata.toString().toRequestBody("application/json".toMediaTypeOrNull())
        try {
            val imageFile = withContext(Dispatchers.IO) {
                uriToFile(image, context).reduceFileSize().resizeIfTooLarge()
            }

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                requestImageFile
            )
            return handleApiException {
                apiService.generateCaption(
                    metadataBody,
                    multipartBody
                )
            }
        } catch (e: Exception) {
            val imageFile = withContext(Dispatchers.IO) {
                downloadImageToFile(context, image.toString())?.resizeIfTooLarge()
                    ?.reduceFileSize()
            }
            imageFile?.let {
                val requestImage = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "image",
                    it.name,
                    requestImage
                )
                return handleApiException {
                    apiService.generateCaption(
                        metadataBody,
                        multipartBody
                    )
                }
            }
            return ApiResponse(false, context.getString(R.string.failed_to_load_image), null)
        }
    }
    override suspend fun getCaptionList(
        token: String,
        context: Context
    ): ApiResponse<List<ListCaptionResponse>> =
        handleApiException { apiService.getCaptionList(token = context.getString(R.string.bearer, token)) }

}