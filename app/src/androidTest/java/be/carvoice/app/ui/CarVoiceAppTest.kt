package be.carvoice.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import be.carvoice.app.model.LaunchAvailability
import be.carvoice.app.model.LauncherState
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class CarVoiceAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun defaultAssistantActionIsAlwaysAvailable() {
        var launches = 0
        var refreshes = 0
        composeRule.setContent {
            CarVoiceApp(
                state = LauncherState(carConnection = "Auto verbonden", audioOutputs = listOf("Autoluidsprekers")),
                onLaunch = { launches++ }, onRefresh = { refreshes++ },
            )
        }
        composeRule.onNodeWithText("Android-standaardassistent").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Start standaardassistent").performScrollTo().assertIsEnabled().performClick()
        composeRule.onNodeWithText("Vernieuw status").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(1, launches)
            assertEquals(1, refreshes)
        }
    }

    @Test
    fun helperExplainsLimitsAndDoesNotRequireChatGpt() {
        composeRule.setContent {
            CarVoiceApp(
                state = LauncherState(availability = LaunchAvailability.NOT_INSTALLED),
                onLaunch = {}, onRefresh = {},
            )
        }
        composeRule.onNodeWithText("Android-standaardassistent").assertIsDisplayed()
        composeRule.onNodeWithText("Android Auto kan deze actie blokkeren.", substring = true).performScrollTo().assertIsDisplayed()
    }
}
