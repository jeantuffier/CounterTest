package no.northernfield.countertest

import android.content.pm.ActivityInfo
import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import no.northernfield.countertest.ui.theme.CounterTestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CounterPresenterTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testRetainedState() {
        composeTestRule.activity.setContent {
            CounterTestTheme {
                CounterScreen(CounterEventBus())
            }
        }

        composeTestRule.onNodeWithTag("counter").assertTextContains("Counter: 0")
        composeTestRule.onNodeWithTag("increment").performClick()
        composeTestRule.onNodeWithTag("counter").assertTextContains("Counter: 1")
        composeTestRule.onNodeWithTag("increment").performClick()
        composeTestRule.onNodeWithTag("counter").assertTextContains("Counter: 2")
        composeTestRule.onNodeWithTag("decrement").performClick()
        composeTestRule.onNodeWithTag("counter").assertTextContains("Counter: 1")
        composeTestRule.onNodeWithTag("reset").performClick()
        composeTestRule.onNodeWithTag("counter").assertTextContains("Counter: 0")

        composeTestRule.onNodeWithTag("increment").performClick()
        composeTestRule.onNodeWithTag("increment").performClick()
        composeTestRule.onNodeWithTag("increment").performClick()
        composeTestRule.onNodeWithTag("counter").assertTextContains("Counter: 3")

        composeTestRule.waitForIdle()
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("counter").assertTextContains("Counter: 3")
    }
}