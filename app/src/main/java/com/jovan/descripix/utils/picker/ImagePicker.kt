package com.jovan.descripix.utils.picker

import android.net.Uri

interface ImagePicker {
    fun launchPicker(onResult: (Uri?) -> Unit)
}