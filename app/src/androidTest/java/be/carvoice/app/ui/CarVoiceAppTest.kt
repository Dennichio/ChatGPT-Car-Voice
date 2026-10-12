package be.carvoice.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
    fun readyStateShowsDetectedVersionAndInvokesLaunchCallback() {
        var launches = 0
        var refreshes = 0
        composeRule.setContent {
            CarVoiceApp(
                state = LauncherState(
                    availability = LaunchAvailability.READY,
                    chatGptVersion = "1.2026.10",
                    carConnection = "Bluetooth: My Car",
                    audioOutputs = listOf("My Car", "Phone speaker"),
                ),
                onLaunch = { launches++ }, onRefresh = { refreshes++ }, onInstall = {},
            )
        }

        composeRule.onNodeWithText("ChatGPT is geïnstalleerd").assertIsDisplayed()
        composeRule.onNodeWithText("Versie 1.2026.10").assertIsDisplayed()
        composeRule.onNodeWithText("Bluetooth: My Car").assertIsDisplayed()
        composeRule.onNodeWithText("Beschikbare audio-uitgangen").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("My Car\nPhone speaker").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open ChatGPT").performScrollTo().assertIsEnabled().performClick()
        composeRule.onNodeWithText("Vernieuw status").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(1, launches)
            assertEquals(1, refreshes)
        }
    }

    @Test
    fun notInstalledStateDisablesLaunchAndOffersOfficialInstall() {
        var installs = 0
        var launches = 0
        composeRule.setContent {
            CarVoiceApp(
                state = LauncherState(availability = LaunchAvailability.NOT_INSTALLED),
                onLaunch = { launches++ }, onRefresh = {}, onInstall = { installs++ },
            )
        }

        composeRule.onNodeWithContentDescription("Open ChatGPT").assertIsNotEnabled()
        composeRule.onNodeWithText("Installeer officiële app").performScrollTo().assertIsDisplayed().performClick()
        composeRule.runOnIdle {
            assertEquals(0, launches)
            assertEquals(1, installs)
        }
    }

    @Test
    fun disabledStateNeverEnablesLaunch() {
        composeRule.setContent {
            CarVoiceApp(
                state = LauncherState(availability = LaunchAvailability.DISABLED),
                onLaunch = {}, onRefresh = {}, onInstall = {},
            )
        }
        composeRule.onNodeWithContentDescription("Open ChatGPT").assertIsNotEnabled()
    }

    @Test
    fun missingLaunchActivityNeverEnablesLaunch() {
        composeRule.setContent {
            CarVoiceApp(
                state = LauncherState(availability = LaunchAvailability.NO_LAUNCH_ACTIVITY),
                onLaunch = {}, onRefresh = {}, onInstall = {},
            )
        }
        composeRule.onNodeWithContentDescription("Open ChatGPT").assertIsNotEnabled()
    }

    @Test
    fun guidanceExplainsVoiceStartAndManualFallback() {
        composeRule.setContent {
            CarVoiceApp(
                state = LauncherState(availability = LaunchAvailability.READY),
                onLaunch = {}, onRefresh = {}, onInstall = {},
            )
        }

        composeRule.onNodeWithText("Spraak starten").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Bij een bestaand gesprek", substring = true).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("CarVoice opent de officiële ChatGPT-app", substring = true).performScrollTo().assertIsDisplayed()
    }
}
