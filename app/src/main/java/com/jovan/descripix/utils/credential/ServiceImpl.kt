package com.jovan.descripix.utils.credential

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jovan.descripix.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IService {
    override suspend fun getGoogleIdToken(context: Context): String {

        return withContext(Dispatchers.Main) {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.client_id))
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(request = request, context = context)

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else {
                throw IllegalStateException("Unexpected credential type")
            }
        }
    }

    override var isTestMode: Boolean
        get() = false
        set(value) {}

    override fun testImagedUrl(): Uri {

        val res = context.resources
        val uri = (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                res.getResourcePackageName(R.drawable.image_dummy) + "/" +
                res.getResourceTypeName(R.drawable.image_dummy) + "/" +
                res.getResourceEntryName(R.drawable.image_dummy)).toUri()
        return uri
    }
}
