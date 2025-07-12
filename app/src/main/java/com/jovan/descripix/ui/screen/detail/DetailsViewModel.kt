package com.jovan.descripix.ui.screen.detail

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.ExifSubIFDDirectory
import com.drew.metadata.exif.GpsDirectory
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.jovan.descripix.R
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.remote.request.CaptionRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.GenerateResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.domain.usecase.DescripixUseCase
import com.jovan.descripix.ui.common.UiState
import com.jovan.descripix.utils.dateFormater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val descripixUseCase: DescripixUseCase,
) : ViewModel() {

    val isConnected = descripixUseCase
        .isConnected()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )
    private val _sessionState: MutableStateFlow<UiState<SessionData>> =
        MutableStateFlow(UiState.Loading)

    val sessionState: StateFlow<UiState<SessionData>>
        get() = _sessionState

    fun getSession(context: Context) {
        viewModelScope.launch {
            _sessionState.value = UiState.Loading
            val connected = isConnected.value
            descripixUseCase.getSession(context, connected)
                .catch { e ->
                    _sessionState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { session ->
                    if (_sessionState.value != UiState.Success(session)) {
                        _sessionState.value = UiState.Success(session)
                    }
                }
        }
    }


    private val _captionEntity = MutableStateFlow<CaptionEntity?>(null)
    val captionEntityState = _captionEntity.asStateFlow()


    fun extractImageMetadata(context: Context, uri: Uri) {
        viewModelScope.launch {
            val captionEntity = internalExtractMetadata(context, uri)
            _captionEntity.value = captionEntity
        }
    }

    private suspend fun internalExtractMetadata(context: Context, uri: Uri): CaptionEntity {
        val contentResolver = context.contentResolver

        var date: Date? = null
        var geoLocation: String? = null
        var device: String? = null
        var model: String? = null
        var author: String? = null

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val metadata = ImageMetadataReader.readMetadata(inputStream)

                val exifSub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
                date = exifSub?.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)

                val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                val location = gpsDirectory?.geoLocation

                if (location != null) {
                    geoLocation = reverseGeocode(context, location.latitude, location.longitude)
                    Log.d("ViewModel", "Location: $geoLocation")
                }

                val ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
                device = ifd0Directory?.getString(ExifIFD0Directory.TAG_MAKE)
                model = ifd0Directory?.getString(ExifIFD0Directory.TAG_MODEL)
                author = ifd0Directory?.getString(ExifIFD0Directory.TAG_ARTIST)
            }
        } catch (e: Exception) {
            Log.e("ImageMetadata", "Error reading metadata", e)
        }

        val dateFormatted = date?.let { dateFormater(it) }

        return CaptionEntity(
            id = -1,
            caption = null,
            author = author ?:"",
            date = dateFormatted ?:"",
            location = geoLocation ?:"",
            device = device ?:"",
            model = model ?:"",
            image = uri.toString()
        )
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun reverseGeocode(context: Context, lat: Double, lon: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            val rawAddress = addresses[0].getAddressLine(0)

                            cont.resume(addresses[0].getAddressLine(0), null)
                        } else cont.resume(null, null)
                    }

                    override fun onError(errorMessage: String?) {
                        cont.resume(null, null)
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            try {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                addresses?.get(0)?.getAddressLine(0)
            } catch (e: IOException) {
                null
            }
        }
    }

    fun clearCaptionEntity() {
        _captionEntity.value = null
    }

    fun setCaptionEntity(captionEntity: CaptionEntity) {
        _captionEntity.value = captionEntity
    }

    private val _loginState: MutableStateFlow<UiState<ApiResponse<LoginResponse>>> =
        MutableStateFlow(UiState.Loading)
    val loginState: StateFlow<UiState<ApiResponse<LoginResponse>>>
        get() = _loginState

    fun login(context: Context) {
        _loginState.value = UiState.Loading

        viewModelScope.launch {
            val result = runCatching {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.client_id))
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                credentialManager.getCredential(request = request, context = context)

            }.onFailure { e ->
                val msg = when (e) {
                    is GetCredentialCancellationException -> "Login Canceled by user"
                    else -> e.message ?: context.getString(R.string.unknown_error)
                }
                _loginState.value = UiState.Error(msg)
                return@launch
            }.getOrNull()

            result?.let {
                when (val credential = result.credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(credential.data)
                                Log.d(
                                    "HomeViewModel",
                                    "Token Google: ${googleIdTokenCredential.idToken}"
                                )
                                val idToken = googleIdTokenCredential.idToken

                                val loginResult = descripixUseCase.login(idToken, context)
                                _loginState.value = UiState.Success(loginResult)

                            } catch (e: GoogleIdTokenParsingException) {
                                _loginState.value = UiState.Error("Token Google tidak valid")
                            }
                        } else {
                            _loginState.value =
                                UiState.Error(context.getString(R.string.unexpected_type_of_credential))
                        }
                    }

                    else -> {
                        _loginState.value =
                            UiState.Error(context.getString(R.string.unexpected_type_of_credential))
                    }
                }
            }
        }
    }

    private val _generatedCaption: MutableStateFlow<UiState<ApiResponse<GenerateResponse>>> =
        MutableStateFlow(UiState.Error("Not Started"))
    val generatedCaption: StateFlow<UiState<ApiResponse<GenerateResponse>>>
        get() = _generatedCaption

    fun generateCaption(
        author: String,
        date: String,
        location: String,
        device: String,
        model: String,
        image: Uri,
        context: Context
    ) {
        _generatedCaption.value = UiState.Loading
        viewModelScope.launch {
            try {
                val metadata = createMetadataJson(
                    author = author,
                    date = date,
                    location = location,
                    device = device,
                    model = model
                )
                Log.d("DetailsViewModel", "generateCaption: $metadata")

                val result = descripixUseCase.generateCaption(metadata, image, context)
                _generatedCaption.value = UiState.Success(result)

            } catch (e: Exception) {
                _generatedCaption.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun createMetadataJson(
        author: String,
        date: String,
        location: String,
        device: String,
        model: String
    ): JSONObject {
        val jsonObject = JSONObject()

        if (author.isNotEmpty()) {
            jsonObject.put("Author", author)
        }

        if (date.isNotEmpty()) {
            jsonObject.put("Date Taken", date)
        }

        if (location.isNotEmpty()) {
            jsonObject.put("Location", location)
        }

        if (device.isNotEmpty()) {
            jsonObject.put("Device", device)
        }

        if (model.isNotEmpty()) {
            jsonObject.put("Model", model)
        }

        return jsonObject
    }


    private val _deleteCaption: MutableStateFlow<UiState<ApiResponse<Unit>>> =
        MutableStateFlow(UiState.Error("Not Started"))
    val deleteCaption: StateFlow<UiState<ApiResponse<Unit>>>
        get() = _deleteCaption

    fun deleteCaption(id: Int, token: String, context: Context){
        _deleteCaption.value = UiState.Loading

        viewModelScope.launch {
            try {
                val result = descripixUseCase.deleteCaption(id, token, context)
                _deleteCaption.value = UiState.Success(result)
            }catch (e: Exception) {
                _deleteCaption.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _saveCaption: MutableStateFlow<UiState<ApiResponse<CaptionDataResponse>>> =
        MutableStateFlow(UiState.Error("Not Started"))
    val saveCaption: StateFlow<UiState<ApiResponse<CaptionDataResponse>>>
        get() = _saveCaption

    fun saveCaption(caption: String, author: String, date: String, location: String, device: String, model: String, image: String, token: String, context: Context){
        _saveCaption.value = UiState.Loading

        viewModelScope.launch {
            try {
                val captionRequest = CaptionRequest(
                    caption = caption,
                    author = author,
                    date = date,
                    location = location,
                    device = device,
                    model = model,
                    image = image
                )
                val result = descripixUseCase.saveCaption(captionRequest, token, context)

                _saveCaption.value = UiState.Success(result)
            }catch (e: Exception) {
                _saveCaption.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _editCaption: MutableStateFlow<UiState<ApiResponse<Unit>>> =
        MutableStateFlow(UiState.Error("Not Started"))
    val editCaption: StateFlow<UiState<ApiResponse<Unit>>>
        get() = _editCaption

    fun editCaption(id: Int, caption: String, author: String, date: String, location: String, device: String, model: String, image: String, token: String, context: Context){
        _editCaption.value = UiState.Loading
        viewModelScope.launch {
            try {
                val captionRequest = CaptionRequest(
                    caption = caption,
                    author = author,
                    date = date,
                    location = location,
                    device = device,
                    model = model,
                    image = image
                )
                val result = descripixUseCase.editCaption(id, captionRequest, token, context)

                _editCaption.value = UiState.Success(result)
            }catch (e: Exception) {
                _editCaption.value = UiState.Error(e.message ?: "Unknown error")
            }
        }

    }


    fun resetAllStates(){
        _sessionState.value = UiState.Loading
        _captionEntity.value = null
        _loginState.value = UiState.Loading
        _generatedCaption.value = UiState.Error("Not Started")
        _deleteCaption.value = UiState.Error("Not Started")
        _editCaption.value = UiState.Error("Not Started")
        _saveCaption.value = UiState.Error("Not Started")
    }
}


