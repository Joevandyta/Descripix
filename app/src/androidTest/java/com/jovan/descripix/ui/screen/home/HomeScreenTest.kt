package com.jovan.descripix.ui.screen.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.jovan.descripix.TestActivity
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.JsonConverter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest{

    private val mockWebServer = MockWebServer()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<TestActivity>()
    private lateinit var navController: TestNavHostController

    @get:Rule(order = 2)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        hiltRule.inject()
        mockWebServer.start(8080)

        composeRule.setContent {
            DescripixTheme {
                navController = TestNavHostController(LocalContext.current)
                navController.navigatorProvider.addNavigator(ComposeNavigator())
                HomeScreen (navigateToDetail = {})
            }
        }
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    @Test
    fun homeScreen_loginSuccess() {
        composeRule.onNodeWithTag(TestTags.GUEST_SCREEN).assertExists()
        val mockResponse = MockResponse()
        .setResponseCode(200)
        .setBody(JsonConverter.readStringFromFile("success_login_response.json"))

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    // 1. Google Login
                    request.path == "/auth/google-login/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("success_login_response.json"))

                    // 3. Refresh Token
                    request.path == "/auth/token-refresh/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("refresh_token_response.json"))

                    // 4. Verify Token
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("caption_detail_response.json"))

                    // 12. Caption List
                    request.path == "/caption/list/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("success_list_caption.json"))
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        mockWebServer.dispatcher = dispatcher

        composeRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithTag(TestTags.AUTHENTICATED_SCREEN) // misalnya HomeScreen muncul
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
    @Test
    fun guestScreen() {

    }

    @Test
    fun autenticatedScreen() {
    }

}