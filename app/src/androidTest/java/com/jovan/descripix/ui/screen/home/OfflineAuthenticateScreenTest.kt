package com.jovan.descripix.ui.screen.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.IdlingRegistry
import com.jovan.descripix.DescripixApp
import com.jovan.descripix.FakeObject
import com.jovan.descripix.TestActivity
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.room.CaptionDao
import com.jovan.descripix.ui.common.TestTags
import com.jovan.descripix.ui.theme.DescripixTheme
import com.jovan.descripix.utils.EspressoIdlingResource
import com.jovan.descripix.utils.conectivity.ConnectivityObserver
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
class OfflineAuthenticateScreenTest {

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
                DescripixApp(navController = navController)
            }
        }
    }
    @Test
    fun home_authenticated_offlineMode_displaysTemporaryCaptions(){
        runBlocking {
            userPreference.saveSession(
                SessionData(
                    refreshToken = "current_refresh",
                    token = "current_token",
                    isLogin = true
                )
            )
            captionDao.insert(FakeObject.listDummy)
        }

        launchScreen()

        composeRule.onNodeWithTag(TestTags.HOME_AUTH_SCREEN).assertExists()
        composeRule.onNodeWithText("offline caption 1").assertExists("Caption not found")
    }
}