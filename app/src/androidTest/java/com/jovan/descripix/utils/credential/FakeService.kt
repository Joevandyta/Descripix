package com.jovan.descripix.utils.credential

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.jovan.descripix.FakeObject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FakeService @Inject constructor(
    @ApplicationContext private val context: Context
) : IService {
    val token = FakeObject.testIdToken
    override suspend fun getGoogleIdToken(context: Context): String {
        Log.d("FakeCredentialService", "getGoogleIdToken called")
        return token
    }

    override var isTestMode: Boolean
        get() = true
        set(value) {}

    override fun testImagedUrl(): Uri {
        return "".toUri()
    }

}