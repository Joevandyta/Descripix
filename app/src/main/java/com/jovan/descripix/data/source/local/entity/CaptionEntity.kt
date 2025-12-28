package com.jovan.descripix.data.source.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class CaptionEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "caption")
    val caption: String?,
    @ColumnInfo(name = "author")
    val author: String? = null,
    @ColumnInfo(name = "date")
    val date: String? = null,
    @ColumnInfo(name = "location")
    val location: String? = null,
    @ColumnInfo(name = "device")
    val device: String? = null,
    @ColumnInfo(name = "model")
    val model: String? = null,
    @ColumnInfo(name = "image")
    val image: String,
): Parcelable

