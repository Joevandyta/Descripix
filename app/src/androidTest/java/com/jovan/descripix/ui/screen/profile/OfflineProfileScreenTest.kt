package com.jovan.descripix.ui.screen.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.IdlingRegistry
import com.jovan.descripix.FakeObject
import com.jovan.descripix.TestActivity
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.entity.UserEntity
import com.jovan.descripix.data.source.local.room.UserDao
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.EspressoIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class OfflineProfileScreenTest {
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

        FakeObject.isConnectedFlow.value = false
        hiltRule.inject()

        System.setProperty("IS_TEST", "true")
        mockWebServer.start(8080)

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

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
    fun authenticateProfile_offlineMode_displaysTemporaryUserData(){
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
                    aboutMe = null,
                    profileImg = "https://lh3.googleusercontent.com/a/ACg8ocIRKZM9Yq2U83R604HPEA3XkRawhJTLT7G73YkxMITK0ROWIw=s96-c"
                )
            )
        }
        launchScreen()
        composeRule.onNodeWithTag(TestTags.PROFILE_AUTH_SCREEN).assertExists()
        composeRule.onNodeWithText("test username").assertExists()
    }
}