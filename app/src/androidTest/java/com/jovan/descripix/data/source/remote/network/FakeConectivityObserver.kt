package com.jovan.descripix.data.source.remote.network

import android.content.Context
import com.jovan.descripix.utils.conectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeConectivityObserver(
    private val context: Context
) : ConnectivityObserver {
    override val isConnected: Flow<Boolean>
        get() = flow { emit(true) }

}