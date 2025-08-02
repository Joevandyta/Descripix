package com.jovan.descripix.ui.screen.home

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.IdlingRegistry
import com.google.common.truth.Truth.assertThat
import com.jovan.descripix.DescripixApp
import com.jovan.descripix.FakeObject
import com.jovan.descripix.R
import com.jovan.descripix.TestActivity
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.entity.CaptionEntity
import com.jovan.descripix.data.source.local.room.CaptionDao
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.EspressoIdlingResource
import com.jovan.descripix.utils.JsonConverter
import com.jovan.descripix.utils.conectivity.ConnectivityObserver
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class AuthenticateScreenTest {

    private val mockWebServer = MockWebServer()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<TestActivity>()
    private lateinit var navController: TestNavHostController

    @get:Rule(order = 2)
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var captionDao: CaptionDao

    @Inject
    lateinit var conectivityObserver: ConnectivityObserver

    @Before
    fun setup() {
        FakeObject.isConnectedFlow.value = true
        hiltRule.inject()

        System.setProperty("IS_TEST", "true")
        mockWebServer.start(8080)

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        composeRule.setContent {
            DescripixTheme {
                navController = TestNavHostController(LocalContext.current)
                navController.navigatorProvider.addNavigator(ComposeNavigator())
                DescripixApp(navController = navController)
            }
        }

        runBlocking {
            userPreference.saveSession(
                SessionData(
                    refreshToken = "current_refresh",
                    token = "current_token",
                    isLogin = true
                )
            )
        }
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

    }

    //    Authenticate Screen

    @Test
    fun home_authenticatedScreen_session_isExpire_refreshToken() {
        var tokenVerifyCallCount = 0

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && tokenVerifyCallCount == 0 -> {
                        tokenVerifyCallCount++
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))
                    }
                    // 2. Refresh Token - success
                    request.path == "/auth/token-refresh/" && request.method == "POST" ->
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("refresh_token_success.json"))

                    // 3. Token Verify - second call (after refresh, should success)
                    request.path == "/auth/token-verify/" && tokenVerifyCallCount > 0 -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))
                    }
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher
        runBlocking {
            assertThat(userPreference.getSession().first().isLogin).isTrue()
        }
        composeRule.onNodeWithTag(TestTags.AUTHENTICATED_SCREEN).assertExists()
        runBlocking {
            assertThat(userPreference.getSession().first().token).isEqualTo("new_access_token")
        }
    }

    @Test
    fun home_authenticatedScreen_refreshToken_isExpire_showGuestScreen() {

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
        runBlocking {
            assertThat(userPreference.getSession().first().isLogin).isTrue()
        }
        composeRule.onNodeWithTag(TestTags.GUEST_SCREEN).assertExists()
        runBlocking {
            assertThat(userPreference.getSession().first().isLogin).isFalse()
        }
    }

    @Test
    fun home_authenticatedScreen_captionListIsEmpty_showsEmptyState() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher
        val expectedText = composeRule.activity.getString(R.string.you_don_t_have_any_captions_yet)
        composeRule.onNodeWithText(expectedText).assertIsDisplayed()
    }

    @Test
    fun home_authenticatedScreen_captionListIsNotEmpty_displaysCaptions() {
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    // 4. Verify Token
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                    // 3. Refresh Token
                    request.path == "/auth/token-refresh/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("refresh_token_success.json"))

                    // 12. Caption List
                    request.path == "/caption/list/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("caption_list_success.json"))

//                    request.path == "/caption/detail/?id=5" && request.method == "GET" ->
//                        MockResponse().setResponseCode(200)
//                            .setBody(JsonConverter.readStringFromFile("caption_detail_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        runBlocking {
            captionDao.insert(FakeObject.listDummy)
            val currentCaption = captionDao.getAllCaption().first()
            assertThat(currentCaption).isNotEmpty()
            assertThat(currentCaption.size).isEqualTo(4)
            assertThat(currentCaption.first().caption).contains("offline")
        }

        composeRule.onNodeWithTag(TestTags.AUTHENTICATED_SCREEN).assertExists()
        composeRule.onNodeWithText("online caption 1").assertExists("Caption not found")

        runBlocking {
            val currentCaption = captionDao.getAllCaption().first()

            assertThat(currentCaption.size).isEqualTo(8)
        }
    }

    @Test
    fun home_authenticatedScreen_clickCaptionItem_displaysDetailCaptions() {

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    // 4. Verify Token
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))
                    // 3. Refresh Token
                    request.path == "/auth/token-refresh/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("refresh_token_success.json"))
                    // 12. Caption List
                    request.path == "/caption/list/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("caption_list_success.json"))
                    request.path == "/caption/detail/?id=1" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("caption_detail_success.json"))
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        composeRule.onNodeWithTag(TestTags.AUTHENTICATED_SCREEN).assertExists()
        composeRule.onNodeWithText("online caption 1").assertExists("Caption not found")
        composeRule.onNodeWithText("online caption 1").performClick()
        composeRule.onNodeWithTag(TestTags.DETAILS_SCREEN).assertExists()
    }

    @Test
    fun uploadImage_AuthenticateMode_ShouldShowDetailScreen(){
        val context = composeRule.activity

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/caption/generate/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("generate_caption_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.menu_upload),
            useUnmergedTree = true).performClick()
        // 4. Wait for processing
        composeRule.waitForIdle()

        //Caption Layout should be hidden
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsNotDisplayed()

        //Should show sign in button, its guestMode, still cant save caption
        val expectedText = context.getString(R.string.save_caption)
        composeRule.onNodeWithContentDescription(expectedText).assertExists()


    }
}