package com.jovan.descripix.data.source.remote.network

import android.content.Context
import com.jovan.descripix.FakeObject
import com.jovan.descripix.utils.conectivity.ConnectivityObserver
import kotlinx.coroutines.flow.Flow

class FakeConectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    override val isConnected: Flow<Boolean>
        get() = FakeObject.isConnectedFlow
}