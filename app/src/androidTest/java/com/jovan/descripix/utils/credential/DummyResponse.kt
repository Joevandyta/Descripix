package com.jovan.descripix.utils.credential

import com.jovan.descripix.utils.JsonConverter
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

object DummyResponse {
    val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when {
                // 1. Google Login
                request.path == "/auth/google-login/" && request.method == "POST" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("login_success.json"))


                // 2. Logout
                request.path == "/auth/logout/" && request.method == "POST" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("caption_detail_success.json"))

                // 3. Refresh Token
                request.path == "/auth/token-refresh/" && request.method == "POST" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("refresh_token_success.json"))

                // 4. Verify Token
                request.path == "/auth/token-verify/" && request.method == "GET" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                // 5. User Detail
                request.path == "/auth/user-detail/" && request.method == "GET" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("get_user_detail_success.json"))

                // 6. Update User Detail
                request.path == "/auth/user-edit/" && request.method == "PUT" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("caption_detail_response.json"))

                // 7. Save Caption
                request.path == "/caption/save/" && request.method == "POST" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("save_caption_success.json"))

                // 8. Edit Caption
                request.path?.startsWith("/caption/detail/") == true && request.method == "PUT" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("update_caption_success.json"))

                // 9. Delete Caption
                request.path?.startsWith("/caption/detail/") == true && request.method == "DELETE" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("delete_caption_success.json"))

                // 10. Caption Detail
                request.path?.startsWith("/caption/detail/") == true && request.method == "GET" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("caption_detail_response.json"))

                // 11. Generate Caption
                request.path == "/caption/generate/" && request.method == "POST" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("generate_caption_success.json"))

                // 12. Caption List
                request.path == "/caption/list/" && request.method == "GET" ->
                    MockResponse().setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("caption_list_success.json"))
                else -> MockResponse().setResponseCode(404)
            }
        }
    }

}

