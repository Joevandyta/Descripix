package com.jovan.descripix.ui.screen.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.IdlingRegistry
import com.jovan.descripix.FakeObject
import com.jovan.descripix.TestActivity
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.room.CaptionDao
import com.jovan.descripix.utils.EspressoIdlingResource
import com.jovan.descripix.utils.conectivity.ConnectivityObserver
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

class ProfileGuestScreenTest {
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
}