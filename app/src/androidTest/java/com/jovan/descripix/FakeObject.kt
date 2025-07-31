package com.jovan.descripix

import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.remote.response.ListCaptionResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

object FakeObject {
    var testIdToken: String = "test_id_token"

    val isConnectedFlow = MutableStateFlow(true)
//
    val listDummy = listOf(
        CaptionEntity(
            id = 1,
            caption = "offline caption 1",
            image = "https://smaller-pictures.appspot.com/images/dreamstime_xxl_65780868_small.jpg"
        ),
        CaptionEntity(
            id = 2,
            caption = "offline caption 2",
            image = "https://smaller-pictures.appspot.com/images/dreamstime_xxl_65780868_small.jpg"
        ),
        CaptionEntity(
            id = 3,
            caption = "offline caption 3",
            image = "https://smaller-pictures.appspot.com/images/dreamstime_xxl_65780868_small.jpg"
        ),
        CaptionEntity(
            id = 4,
            caption = "offline caption 4",
            image = "https://png.pngtree.com/png-vector/20191121/ourmid/pngtree-blue-bird-vector-or-color-illustration-png-image_2013004.jpg"
        )
    )

}