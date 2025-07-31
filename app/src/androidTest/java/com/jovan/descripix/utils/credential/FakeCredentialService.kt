package com.jovan.descripix.utils.credential

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FakeCredentialService @Inject constructor(
    @ApplicationContext private val context: Context
) : ICredentialService {

    override suspend fun getGoogleIdToken(context: Context): String {
        Log.d("FakeCredentialService", "getGoogleIdToken called")
        return "test_id_token"
    }
}