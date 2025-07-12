package com.jovan.descripix.data.source.remote.response

data class CaptionDataResponse(
    val id: Int,
    val caption: String,
    val author: String?, // nullable
    val date: String?,
    val location: String?,
    val device: String?, // nullable
    val model: String?, // nullable
    val image: String,
    val uid: String
)
