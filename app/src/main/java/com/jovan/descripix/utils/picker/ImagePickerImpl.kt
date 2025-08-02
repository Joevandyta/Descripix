package com.jovan.descripix.utils.picker

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher

class ImagePickerImpl(
    private val launcher: ManagedActivityResultLauncher<Array<String>, Uri?>
): ImagePicker {
    override fun launchPicker(onResult: (Uri?) -> Unit) {
        launcher.launch(arrayOf("image/*"))
    }
}