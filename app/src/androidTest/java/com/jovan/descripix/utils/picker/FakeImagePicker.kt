package com.jovan.descripix.utils.picker

import android.net.Uri

class FakeImagePicker(private val fakeUri: Uri?): ImagePicker {
    override fun launchPicker(onResult: (Uri?) -> Unit) {
        onResult(fakeUri)
    }
}