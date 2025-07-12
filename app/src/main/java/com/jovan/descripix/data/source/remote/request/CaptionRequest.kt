package com.jovan.descripix.data.source.remote.request

data class CaptionRequest(
    val caption: String,
    val author: String?,
    val date: String?,
    val location: String?,
    val device: String?,
    val model: String?,
    val image: String,
)
