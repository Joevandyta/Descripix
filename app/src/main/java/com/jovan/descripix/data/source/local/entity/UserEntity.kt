package com.jovan.descripix.data.source.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class UserEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id : String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "email")
    val email: String ,
    @ColumnInfo(name = "gender")
    val gender: String? = null,
    @ColumnInfo(name = "birth_date")
    val birthDate: String? = null,
    @ColumnInfo(name = "about_me")
    val aboutMe: String? = null,
    @ColumnInfo(name = "profile_img")
    val profileImg: String? = null,
):Parcelable