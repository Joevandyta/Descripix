package com.jovan.descripix.data.source.remote.response

data class ApiResponse<T>(
    val status: Boolean,
    val message: Any,
    val data: T?
)
