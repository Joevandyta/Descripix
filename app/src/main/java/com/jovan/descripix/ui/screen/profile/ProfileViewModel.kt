package com.jovan.descripix.ui.screen.profile

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jovan.descripix.R
import com.jovan.descripix.ui.common.UiState
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.remote.request.UserRequest
import com.jovan.descripix.data.source.remote.response.ApiResponse
import com.jovan.descripix.domain.model.Language
import com.jovan.descripix.domain.usecase.DescripixUseCase
import com.jovan.descripix.utils.convertBirthDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val descripixUseCase: DescripixUseCase) :
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
                .catch { e ->
                    _sessionState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { data ->
                    _sessionState.value = UiState.Success(data)
                }
        }
    }

    private val _logoutState: MutableStateFlow<UiState<ApiResponse<Unit>>> =
        MutableStateFlow(UiState.Error("Not Started"))
    val logoutState: StateFlow<UiState<ApiResponse<Unit>>>
        get() = _logoutState

    fun logout(context: Context) {
        viewModelScope.launch {
            _logoutState.value = UiState.Loading

            val refreshToken = (sessionState.value as UiState.Success).data.refreshToken
            try {
                val response = descripixUseCase.logout(refreshToken, context)
                _logoutState.value = UiState.Success(response)
            } catch (e: Exception) {
                _logoutState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }


    private val _userDetailState: MutableStateFlow<UiState<UserEntity>> =
        MutableStateFlow(UiState.Loading)
    val userDetailState: StateFlow<UiState<UserEntity>>
        get() = _userDetailState

    fun getUserDetail(connected: Boolean, refreshToken: String, token: String, context: Context) {
        viewModelScope.launch {
            _userDetailState.value = UiState.Loading

            descripixUseCase.getUserDetail(connected, refreshToken, token, context)
                .catch { e ->
                    _userDetailState.value = UiState.Error(e.message ?: "Unknown error")
                }
                .collect { data ->
                    val birthDate = data.birthDate?.convertBirthDate()
                    var userGender = data.gender
                    if (data.gender == context.getString(R.string.string_fix_male)) {
                        userGender = context.getString(R.string.string_display_male)
                    } else if (data.gender == context.getString(R.string.string_fix_female)) {
                        userGender = context.getString(R.string.string_display_female)
                    }
                    val fixUser = data.copy(
                        gender = userGender,
                        birthDate = birthDate
                    )
                    _userDetailState.value = UiState.Success(fixUser)
                }
        }
    }

    private val _updateUserState: MutableStateFlow<UiState<ApiResponse<Unit>>> =
        MutableStateFlow(UiState.Error("Not Started"))
    val updateUserState: StateFlow<UiState<ApiResponse<Unit>>>
        get() = _updateUserState

    fun updateUserDetail(userRequest: UserRequest, token: String, context: Context) {
        viewModelScope.launch {
            _updateUserState.value = UiState.Loading
            Log.d("ProfileViewModel Try", "updateUserDetail")

            viewModelScope.launch {
                try {
                    val userBirthDate = userRequest.birthDate?.convertBirthDate()
                    Log.d("ProfileViewModel-User Try", "updateUserDetail start")

                    var userGender = userRequest.gender
                    if (userRequest.gender == context.getString(R.string.string_display_male)) {
                        userGender = context.getString(R.string.string_fix_male)
                    } else if (userRequest.gender == context.getString(R.string.string_display_female)) {
                        userGender = context.getString(R.string.string_fix_female)
                    }
                    val fixUser = UserRequest(
                        gender = userGender,
                        birthDate = userBirthDate,
                        aboutMe = userRequest.aboutMe
                    )

                    val response = descripixUseCase.updateUserDetail(fixUser, token, context)
                    Log.d("ProfileViewModel-User Try", "updateUserDetail : $response")

                    _updateUserState.value = UiState.Success(response)
                } catch (e: Exception) {
                    Log.e("ProfileViewModel-User Try", "Error wak")
                    _updateUserState.value = UiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun resetAllStates() {
        _sessionState.value = UiState.Loading
        _logoutState.value = UiState.Error("Not Started")
        _userDetailState.value = UiState.Loading
        _updateUserState.value = UiState.Error("Not Started")
    }

    //Language
    private var _selectedLanguage: MutableStateFlow<UiState<Language>> =
        MutableStateFlow(UiState.Loading)
    val selectedLanguage: StateFlow<UiState<Language>>
        get() = _selectedLanguage

    fun initializeLanguage(context: Context) {
        _selectedLanguage.value = UiState.Loading
        try {
            val language = getLanguageCode(context)

            if (language == "id") {
                _selectedLanguage.value = UiState.Success(Language.Indonesia)
            } else {
                _selectedLanguage.value = UiState.Success(Language.English)
            }
        } catch (e: Exception) {
            _selectedLanguage.value = UiState.Error(e.message ?: "Unknown error")
        }
    }

    fun changeLanguage(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(languageCode)
        }else{
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        }
    }

    private fun getLanguageCode(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales[0]?.toLanguageTag()
                ?.split("-")?.first() ?: "en"
        } else {
            AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()?.split("-")?.first()
                ?: "en"
        }
    }
}