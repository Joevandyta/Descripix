package com.jovan.descripix.data.source.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity
@Parcelize
data class CaptionEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "caption")
    val caption: String?,
    @ColumnInfo(name = "author")
    val author: String?,
    @ColumnInfo(name = "date")
    val date: String?,
    @ColumnInfo(name = "location")
    val location: String?,
    @ColumnInfo(name = "device")
    val device: String?,
    @ColumnInfo(name = "model")
    val model: String?,
    @ColumnInfo(name = "image")
    val image: String,






): Parcelable

