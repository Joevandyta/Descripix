package com.jovan.descripix.data.source.remote.network

import android.content.Context
import com.jovan.descripix.FakeObject
import com.jovan.descripix.utils.conectivity.ConnectivityObserver
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeConectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    override val isConnected: Flow<Boolean>
        get() = FakeObject.isConnectedFlow
}