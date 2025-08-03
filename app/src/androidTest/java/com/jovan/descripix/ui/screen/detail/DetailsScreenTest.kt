package com.jovan.descripix.ui.screen.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.IdlingRegistry
import com.jovan.descripix.DescripixApp
import com.jovan.descripix.FakeObject
import com.jovan.descripix.R
import com.jovan.descripix.TestActivity
import com.jovan.descripix.data.source.local.datastore.SessionData
import com.jovan.descripix.data.source.local.datastore.UserPreference
import com.jovan.descripix.data.source.local.entity.CaptionEntity
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
class DetailsScreenTest {

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

    private fun launchDetailScreen(captionEntity: CaptionEntity){
        composeRule.setContent {
            DescripixTheme {
                DetailScreen(
                    captionEntity = captionEntity,
                    onBack = {},
                )
            }
        }
    }
    @Test
    fun generateCaption_forFirstTime() {
        val context = composeRule.activity

        //Start detail screen from by uploading Image from gallery
        launchDetailScreen(FakeObject.uploadCaptionEntity)

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))

                    request.path == "/caption/generate/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("generate_caption_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Caption Still Empty, and layout caption is hidden
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsNotDisplayed()

        val expectedText = context.getString(R.string.generate_caption)
        composeRule.onNodeWithContentDescription(expectedText).assertExists()

        //Click Generate Button
        composeRule.onNodeWithContentDescription(expectedText).performClick()

        //Caption is Generated Successfully, Caption Layout is Displayed
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsDisplayed()
        composeRule.onNodeWithText("Caption Body is Generated Successfully").assertExists()
    }

    @Test
    fun regenerateCaption_shouldShowNewCaption() {
        launchDetailScreen(FakeObject.savedCaptionEntity)

        val context = composeRule.activity
        //Start detail screen from by uploading Image from gallery
        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))

                    request.path == "/caption/generate/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("generate_caption_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Caption is already filled, and layout caption is displayed
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsDisplayed()
        composeRule.onNodeWithText("saved caption").assertExists()


        //Click Generate Button
        val expectedText = context.getString(R.string.generate_caption)
        composeRule.onNodeWithContentDescription(expectedText).assertExists()
        composeRule.onNodeWithContentDescription(expectedText).performClick()

        //Caption is Generated Successfully, Caption Layout is Displayed
        composeRule.onNodeWithText("Caption Body is Generated Successfully").assertExists()
    }

    @Test
    fun saveCaption_shouldChangeSaveButtonState() {
        val context = composeRule.activity

        launchDetailScreen(FakeObject.uploadCaptionEntity)

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))

                    request.path == "/caption/generate/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("generate_caption_success.json"))

                    request.path == "/caption/save/" && request.method == "POST" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("save_caption_success.json"))

                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Caption Still Empty, and layout caption is hidden
        val textGenerateCaption = context.getString(R.string.generate_caption)
        //Generate Caption
        composeRule.onNodeWithContentDescription(textGenerateCaption).performClick()

        //Caption is Generated Successfully, Caption Layout is Displayed
        composeRule.onNodeWithTag(TestTags.CAPTION_TEXT_LAYOUT).assertIsDisplayed()

        //Save Caption Button is Displayed
        val textCaptionLayout = context.getString(R.string.save_caption)
        composeRule.onNodeWithContentDescription(textCaptionLayout).assertExists()

        //if Caption is still not saved, save button is displayed
        val textSave = context.getString(R.string.save)
        val textSaved = context.getString(R.string.saved)
        composeRule.onNodeWithText(textSaved).assertDoesNotExist()
        composeRule.onNodeWithText(textSave).assertExists()
        composeRule.onNodeWithText(textSave).performClick()

        //After Save, save button is changed to saved
        composeRule.onNodeWithText(textSave).assertDoesNotExist()
        composeRule.onNodeWithText(textSaved).assertExists()

    }

    @Test
    fun updateSavedCaptionResult_shouldFetchNewData(){
        val context = composeRule.activity
        val fakeCaption = FakeObject.savedCaptionEntity
        launchDetailScreen(fakeCaption)

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))

                    request.path?.startsWith("/caption/detail/") == true && request.method == "PUT" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("update_caption_success.json"))


                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Test if caption and metadata is have value
        composeRule.onNodeWithText(fakeCaption.caption.toString(), substring = true).assertExists()

        //Click Metadata Drop down
        composeRule.onNodeWithContentDescription(context.getString(R.string.show_more)).performClick()

        composeRule.onNodeWithText(fakeCaption.author.toString(), substring = true).assertExists()
        composeRule.onNodeWithText(fakeCaption.date.toString(), substring = true).assertExists()
        composeRule.onNodeWithText(fakeCaption.location.toString(), substring = true).assertExists()
        composeRule.onNodeWithText(fakeCaption.device.toString(), substring = true).assertExists()
        composeRule.onNodeWithText(fakeCaption.model.toString(), substring = true).assertExists()

        //Before Edit, check save button is on "Saved" state
        val textSaved = context.getString(R.string.saved)
        val textSave = context.getString(R.string.save)

        composeRule.onNodeWithText(textSaved).assertExists()
        composeRule.onNodeWithText(textSaved).assertIsDisplayed()
        composeRule.onNodeWithText(textSave).assertDoesNotExist()


        //Edit Some Data in textField
        composeRule.onNodeWithText(fakeCaption.author.toString(), substring = true).performTextInput("New Caption")
        composeRule.onNodeWithText(fakeCaption.device.toString(), substring = true).performTextInput("New Device")
        composeRule.onNodeWithText(fakeCaption.model.toString(), substring = true).performTextInput("New Model")

        composeRule.onNodeWithText("New Caption", substring = true).assertExists()
        composeRule.onNodeWithText("New Device", substring = true).assertExists()
        composeRule.onNodeWithText("New Model", substring = true).assertExists()

        //The Save Button Will Change to "Save" State
        //Before Edit, check save button is on "Saved" state
        composeRule.onNodeWithText(textSave).assertExists()
        composeRule.onNodeWithText(textSave).assertIsDisplayed()
        composeRule.onNodeWithText(textSaved).assertDoesNotExist()


        //Click the Button
        composeRule.onNodeWithText(textSave).performClick()

        //Button State Change to "Saved" state, update Success
        composeRule.onNodeWithText(textSaved).assertExists()
        composeRule.onNodeWithText(textSaved).assertIsDisplayed()
    }

    @Test
    fun deleteSavedCaptionResult_shouldFetchNewData(){
        val context = composeRule.activity
        val fakeCaption = FakeObject.savedCaptionEntity
        launchDetailScreen(fakeCaption)

        val dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path == "/auth/token-verify/" && request.method == "GET" ->
                        MockResponse()
                            .setResponseCode(401)
                            .setBody(JsonConverter.readStringFromFile("token_verify_failed.json"))

                    request.path?.startsWith("/caption/detail/") == true && request.method == "DELETE" ->
                        MockResponse().setResponseCode(200)
                            .setBody(JsonConverter.readStringFromFile("delete_caption_success.json"))


                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        mockWebServer.dispatcher = dispatcher

        //Test if caption and metadata is have value
        composeRule.onNodeWithText(fakeCaption.caption.toString(), substring = true).assertExists()

        val textSaved = context.getString(R.string.saved)
        val textSave = context.getString(R.string.save)

        //Before Delete, check save button is on "Saved" state.
        composeRule.onNodeWithText(textSaved).assertExists()
        composeRule.onNodeWithText(textSaved).assertIsDisplayed()
        composeRule.onNodeWithText(textSave).assertDoesNotExist()

        //        Click the Button
        composeRule.onNodeWithText(textSaved).performClick()

        //Button State Change to "Saved" state, update Success
        composeRule.onNodeWithText(textSaved).assertDoesNotExist()
        composeRule.onNodeWithText(textSave).assertExists()
        composeRule.onNodeWithText(textSave).assertIsDisplayed()
    }
}