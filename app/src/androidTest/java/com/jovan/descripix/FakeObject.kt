package com.jovan.descripix

import com.jovan.descripix.data.source.local.entity.CaptionEntity
import kotlinx.coroutines.flow.MutableStateFlow

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

    val savedCaptionEntity = CaptionEntity(
        id = 1,
        caption = "saved caption",
        author = "Test Author",
        date = "2023-10-10",
        location = "Test Location",
        device = "Test Device",
        model = "Test Model",
        image = "https://smaller-pictures.appspot.com/images/dreamstime_xxl_65780868_small.jpg"
        ,
    )

    val uploadCaptionEntity = CaptionEntity(
        id = -1,
        caption = null,
        author = "Test Author",
        date = "",
        location = "Test Location",
        device = "",
        model = "Test Model",
        image = "https://smaller-pictures.appspot.com/images/dreamstime_xxl_65780868_small.jpg"
        ,
    )
}