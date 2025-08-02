package com.jovan.descripix.utils.credential

import android.content.Context
import android.net.Uri

interface IService {
    suspend fun getGoogleIdToken(context: Context): String

    var isTestMode: Boolean

    fun testImagedUrl(): Uri
}
