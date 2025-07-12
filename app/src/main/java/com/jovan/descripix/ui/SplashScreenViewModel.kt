package com.jovan.descripix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jovan.descripix.domain.usecase.DescripixUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(private val descripixUseCase: DescripixUseCase): ViewModel() {

    private val mutableStateFlow = MutableStateFlow(true)
    val isLoading = mutableStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            delay(3000)
            mutableStateFlow.value = false
        }
    }
}