package com.jovan.descripix.ui.screen.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.IdlingRegistry
import com.jovan.descripix.DescripixApp
import com.jovan.descripix.R
import com.jovan.descripix.TestActivity
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.navigation.Screen
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.EspressoIdlingResource
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
class GuestScreenTest {

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
        System.setProperty("IS_TEST", "true")
        mockWebServer.start(8080)

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" -> {
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))
                    }
                    // 2. Refresh Token - success
                    request.path == "/auth/token-refresh/" && request.method == "POST" ->
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("refresh_token_failed.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        composeRule.setContent {
            DescripixTheme {
                navController = TestNavHostController(LocalContext.current)
                navController.navigatorProvider.addNavigator(ComposeNavigator())
                DescripixApp(navController = navController)
            }
        }

    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }
    //    Authenticate Screen
    @Test
    fun home_guestScreen_whenSessionNotAvailable_shouldShowGuestUI() {
        composeRule.onNodeWithTag(TestTags.GUEST_SCREEN).assertExists()
    }

    @Test
    fun uploadImage_GuestMode_ShouldShowDetailScreen() {
        val context = composeRule.activity

        composeRule.onNodeWithTag(
            Screen.Upload.route + TestTags.BOTTOM_BAR_ICON,
            useUnmergedTree = true
        ).assertExists()

        composeRule.onNodeWithTag(
            Screen.Upload.route + TestTags.BOTTOM_BAR_ICON,
            useUnmergedTree = true
        ).performClick()
        //Wait for processing
        composeRule.waitForIdle()

        //Caption Layout should be hidden
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsNotDisplayed()

        //Should show sign in button, its guestMode, still cant save caption
        var expectedText = context.getString(R.string.sign_in_button)
        composeRule.onNodeWithContentDescription(expectedText).assertExists()
    }

    @Test
    fun home_loginFlow_shouldShowAuthenticatedScreen() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/google-login/" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("login_success.json"))

                    request.path == "/auth/token-verify/" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        composeRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON).performClick()
        composeRule.onNodeWithTag(TestTags.AUTHENTICATED_SCREEN).assertExists()
    }
}