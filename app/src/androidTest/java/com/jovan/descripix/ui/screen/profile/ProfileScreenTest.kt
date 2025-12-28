package com.jovan.descripix.ui.screen.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.IdlingRegistry
import com.jovan.descripix.FakeObject
import com.jovan.descripix.R
import com.jovan.descripix.TestActivity
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.local.room.UserDao
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.EspressoIdlingResource
import com.jovan.descripix.utils.JsonConverter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
class ProfileScreenTest {
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
    lateinit var userDao: UserDao

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

            userDao.insertUser(
                UserEntity(
                    id = "current_refresh",
                    username = "test username",
                    email = "test@gmail.com",
                    gender = "male",
                    birthDate = null,
                    aboutMe = "start About Me",
                    profileImg = "https://lh3.googleusercontent.com/a/ACg8ocIRKZM9Yq2U83R604HPEA3XkRawhJTLT7G73YkxMITK0ROWIw=s96-c"
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
                ProfileScreen()
            }
        }
    }
    @Test
    fun authenticateProfileScreen_displaysUserData(){

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))
                    }

                    request.path == "/auth/user-detail/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("get_user_detail_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        launchScreen()
        composeRule.onNodeWithTag(TestTags.PROFILE_AUTH_SCREEN).assertExists()
        composeRule.onNodeWithText("test username").assertExists()
    }

    @Test
    fun profile_logout_showsGuestUI(){
        val context = composeRule.activity
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))

                    request.path == "/auth/user-detail/" && request.method == "GET" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("get_user_detail_success.json"))

                    request.path == "/auth/logout/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("caption_detail_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        val textLogout = context.getString(R.string.logout)
        mockWebServer.dispatcher = dispatcher
        launchScreen()
        composeRule.onNodeWithTag(TestTags.PROFILE_AUTH_SCREEN).assertExists()
        composeRule.onNodeWithText(textLogout).assertExists().performClick()

        composeRule.onNodeWithTag(TestTags.LOGOUT_MODAL).assertExists()

        composeRule.onNodeWithTag(TestTags.LOGOUT_CONFIRM).performClick()
        composeRule.onNodeWithTag(TestTags.PROFILE_GUEST_SCREEN).assertExists()
    }

    @Test
    fun profile_updateUserDetails_fetchesUpdatedProfile (){
        val context = composeRule.activity
        var requestDetailCount = 0

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" -> {
                        MockResponse()
                            .setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("token_verify_success.json"))
                    }

                    request.path == "/auth/user-detail/" && requestDetailCount == 0 -> {
                        requestDetailCount++
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("get_user_detail_success.json"))
                    }
                    request.path == "/auth/user-edit/" && request.method == "PUT" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("update_profile_success.json"))

                    request.path == "/auth/user-detail/" && requestDetailCount >= 0 -> {
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("updated_user_detail_success.json"))
                    }
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        val textEditProfile = context.getString(R.string.edit_profile)

        mockWebServer.dispatcher = dispatcher

        launchScreen()
        composeRule.onNodeWithTag(TestTags.PROFILE_AUTH_SCREEN).assertExists()

        //Check started aboutme Text
        composeRule.onNodeWithText("start About Me", ignoreCase = true).assertExists()

        //Click Edit Profile
        composeRule.onNodeWithContentDescription(textEditProfile).assertExists().performClick()

        //Edit Profile Modal displayed
        composeRule.onNodeWithTag(TestTags.EDIT_PROFILE_MODAL).assertExists()

        //Replace the text to new text
        composeRule.onNodeWithTag(TestTags.EDIT_ABOUT_ME)
            .assertExists()
            .performTextReplacement("Updated About Me")

        //Save New Data
        composeRule.onNodeWithTag(TestTags.EDIT_PROFILE_SUBMIT).assertExists().performClick()


        composeRule.onNodeWithTag(TestTags.EDIT_PROFILE_MODAL).assertDoesNotExist()

        composeRule.onNodeWithText("Updated About Me", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("start About Me", ignoreCase = true).assertDoesNotExist()
    }
}