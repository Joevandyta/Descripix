package com.jovan.descripix.ui.screen.home

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.jovan.descripix.R
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.remote.network.ConnectivityObserver
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.domain.usecase.DescripixUseCase
import com.jovan.descripix.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val descripixUseCase: DescripixUseCase
) :
    ViewModel() {

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

    fun getSession(connected: Boolean, context: Context) {
        viewModelScope.launch {
            _sessionState.value = UiState.Loading
            Log.d("HomeViewModel", "isConnected: $connected")
            descripixUseCase.getSession(context, connected)
                .distinctUntilChanged()
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
                    is GetCredentialCancellationException -> context.getString(R.string.login_canceled_by_user)
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
                                _loginState.value = UiState.Error(context.getString(R.string.google_id_is_not_valid))
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

    private val _captionListState: MutableStateFlow<UiState<List<CaptionEntity>>> =
        MutableStateFlow(UiState.Loading)
    val captionListState: StateFlow<UiState<List<CaptionEntity>>>
        get() = _captionListState

    fun getAllCaptions(connected: Boolean, token: String, context: Context) {
        _captionListState.value = UiState.Loading
        viewModelScope.launch {
            descripixUseCase.getAllCaptions(connected, token, context)
                .distinctUntilChanged()
                .catch { e ->
                    _captionListState.value = UiState.Error(e.message ?: "Unknown error")
                    Log.e("UiState.Error", "${e.message}")
                }
                .collect { data ->
                    _captionListState.value = UiState.Success(data)
                }
        }
    }


    private val _captionDetailState: MutableStateFlow<UiState<ApiResponse<CaptionDataResponse>>> =
        MutableStateFlow(UiState.Error("Not Started"))
    val captionDetailState: StateFlow<UiState<ApiResponse<CaptionDataResponse>>>
        get() = _captionDetailState

    fun getCaptionDetail(id: Int, context: Context) {
        _captionDetailState.value = UiState.Loading

        val currentSession = sessionState.value
        if (currentSession is UiState.Success) {
            viewModelScope.launch {
                try {
                    val response =
                        descripixUseCase.getCaptionDetails(id, currentSession.data.token, context)
                    Log.d("HomeViewModel-getCaptionDetail", "getCaptionDetail: $response")
                    _captionDetailState.value = UiState.Success(response)
                } catch (e: Exception) {
                    _captionDetailState.value = UiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun resetCaptionDetail() {
        _captionDetailState.value = UiState.Error("Not Started")
    }


    fun resetAllStates(){
        _sessionState.value = UiState.Loading
        _loginState.value = UiState.Loading
        _captionListState.value = UiState.Loading
        _captionDetailState.value = UiState.Error("Not Started")
    }
}