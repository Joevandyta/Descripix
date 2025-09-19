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
import com.jovan.descripix.ui.screen.detail.DetailScreen
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

    private fun launchScreen(){
        composeRule.setContent {
            DescripixTheme {
                navController = TestNavHostController(LocalContext.current)
                navController.navigatorProvider.addNavigator(ComposeNavigator())
                DescripixApp(navController = navController)
            }
        }
    }
    //    Authenticate Screen
    @Test
    fun home_authenticated_sessionExpired_refreshesToken() {
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

        //Launch Screen
        launchScreen()

        composeRule.onNodeWithTag(TestTags.HOME_AUTH_SCREEN).assertExists()
        runBlocking {
            assertThat(userPreference.getSession().first().token).isEqualTo("new_access_token")
        }
    }

    @Test
    fun home_authenticated_refreshTokenExpired_showsGuestUI() {

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" -> {
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))
                    }

                    request.path == "/auth/token-refresh/" && request.method == "POST" ->
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("refresh_token_failed.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Data Before
        runBlocking {
            assertThat(userPreference.getSession().first().isLogin).isTrue()
        }

        //Launch Screen
        launchScreen()


        composeRule.onNodeWithTag(TestTags.GUEST_SCREEN).assertExists()
        runBlocking {
            assertThat(userPreference.getSession().first().isLogin).isFalse()
        }
    }

    @Test
    fun home_authenticated_emptyCaptions_showsEmptyState() {

        //Its run on authenticateScreen
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                    request.path == "/caption/list/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("caption_list_empty.json"))
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Launch Screen
        launchScreen()

        val expectedText = composeRule.activity.getString(R.string.you_don_t_have_any_captions_yet)
        composeRule.onNodeWithText(expectedText).assertIsDisplayed()
    }

    @Test
    fun home_authenticated_captionsExist_displaysCaptions() {

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                    request.path == "/caption/list/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("caption_list_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher
        var initialLocalSize = 0
        runBlocking {
            captionDao.insert(FakeObject.listDummy)
            val localCaption = captionDao.getAllCaption().first()
            initialLocalSize = localCaption.size
            assertThat(localCaption).isNotEmpty()
            assertThat(initialLocalSize).isEqualTo(4)
            assertThat(localCaption.first().caption).contains("offline")
        }

        //Launch Screen
        launchScreen()

        composeRule.onNodeWithTag(TestTags.HOME_AUTH_SCREEN).assertExists()
        composeRule.onNodeWithText("online caption 1").assertExists("Caption not found")

        runBlocking {
            val currentCaption = captionDao.getAllCaption().first()
            assertThat(currentCaption.size).isNotEqualTo(initialLocalSize)
        }
    }

    @Test
    fun home_authenticated_clickCaptionItem_opensCaptionDetail() {

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" -> MockResponse()
                        .setResponseCode(200)
                        .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

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

        //Launch Screen
        launchScreen()

        composeRule.onNodeWithTag(TestTags.HOME_AUTH_SCREEN).assertExists()
        composeRule.onNodeWithText("online caption 1").assertExists("Caption not found")
        composeRule.onNodeWithText("online caption 1").performClick()
        composeRule.onNodeWithTag(TestTags.DETAILS_SCREEN).assertExists()
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsDisplayed()
    }

    @Test
    fun uploadImage_authenticatedMode_opensDetailScreen_showsSaveCaptionButton(){
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Launch Screen
        launchScreen()

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.menu_upload),
            useUnmergedTree = true).performClick()
        // 4. Wait for processing
        composeRule.waitForIdle()

        //Caption Layout should be hidden
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsNotDisplayed()
        //Should show save button, its authMode, already can save caption
        composeRule.onNodeWithTag(TestTags.FLOATING_TOOLBAR_SAVE).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON).assertIsNotDisplayed()
    }
}