package com.jovan.descripix.data.source.remote.response

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @field:SerializedName("id")
    val id : String,
    val username: String,
    val email: String,
    val gender: String,
    @field:SerializedName("birth_date")
    val birthDate: String,
    @field:SerializedName("about_me")
    val aboutMe: String,
    @field:SerializedName("profile_img")
    val profileImg: String,
)
