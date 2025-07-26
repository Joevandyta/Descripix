package com.jovan.descripix.data.source.local.datastore

data class SessionData(
    val refreshToken: String,
    val token: String,
    val isLogin: Boolean = false
){
    companion object {
        fun empty() = SessionData("", "", false)
    }
}
