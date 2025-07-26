package com.jovan.descripix.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jovan.descripix.R

fun shareContent(
    context: Context,
    text: String,
    imageUri: Uri,
    chooserTitle: String = context.getString(R.string.share_via)
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        // Share both image and text
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_content))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

fun shareToSpecificApp(
    context: Context,
    packageName: String,
    text: String,
    imageUri: Uri
) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setPackage(packageName)
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    } catch (e: Exception) {
        // App not installed, fallback to general share
        shareContent(context, text, imageUri)
    }
}