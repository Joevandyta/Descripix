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
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.data.source.remote.response.CaptionDataResponse
import com.jovan.descripix.data.source.remote.response.LoginResponse
import com.jovan.descripix.domain.usecase.DescripixUseCase
import com.jovan.descripix.ui.common.UiState
import com.jovan.descripix.utils.credential.CredentialService
import com.jovan.descripix.utils.credential.ICredentialService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val credentialService: ICredentialService,
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
            descripixUseCase.getSession(context, connected)
                .distinctUntilChanged()
                .catch { e ->
                    _sessionState.value = UiState.Error(e.message ?: context.getString(R.string.unknown_error))
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
        Log.d("HomeViewModel - login", "Run")
        viewModelScope.launch {
            try {
                val idToken = credentialService.getGoogleIdToken(context)
                val loginResult = descripixUseCase.login(idToken, context)
                _loginState.value = UiState.Success(loginResult)
                Log.d("HomeViewModel - login", "Success")

            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Unknown error")
                Log.e("HomeViewModel - login", "Error")

            }

        }
    }

    private val _captionListState: MutableStateFlow<UiState<List<CaptionEntity>>> =
        MutableStateFlow(UiState.Loading)
    val captionListState: StateFlow<UiState<List<CaptionEntity>>>
        get() = _captionListState

    fun getAllCaptions(connected: Boolean, token: String, context: Context) {
        _captionListState.value = UiState.Loading
        Log.e("HomeViewModel - getAllCaptions", "Run")
        viewModelScope.launch {
            descripixUseCase.getAllCaptions(connected, token, context)
                .distinctUntilChanged()
                .catch { e ->
                    _captionListState.value = UiState.Error(e.message ?: context.getString(R.string.unknown_error))
                    Log.e("UiState.Error", "${e.message}")
                }
                .collect { data ->
                    Log.e("HomeViewModel - getAllCaptions", "${data.size}")

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
                    _captionDetailState.value = UiState.Success(response)
                } catch (e: Exception) {
                    _captionDetailState.value = UiState.Error(e.message ?: context.getString(R.string.unknown_error))
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