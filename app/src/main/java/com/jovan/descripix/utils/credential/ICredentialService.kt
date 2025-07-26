package com.jovan.descripix.utils.credential

import android.content.Context

interface ICredentialService {
    suspend fun getGoogleIdToken(context: Context): String
}
