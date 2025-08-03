package com.jovan.descripix.utils.credential

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.jovan.descripix.FakeObject
import com.jovan.descripix.R
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
        val res = context.resources
        val url = "https://smaller-pictures.appspot.com/images/dreamstime_xxl_65780868_small.jpg"
        val uri = (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                res.getResourcePackageName(R.drawable.image_dummy) + "/" +
                res.getResourceTypeName(R.drawable.image_dummy) + "/" +
                res.getResourceEntryName(R.drawable.image_dummy)).toUri()
        return uri
    }

}