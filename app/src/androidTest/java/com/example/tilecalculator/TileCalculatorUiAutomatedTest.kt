package com.example.tilecalculator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TileCalculatorUiAutomatedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFullAppWorkflow() {
        // Wait for Splash screen animation (~1.4s) to transition to Calculator screen
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(hasText("FLOOR AREA")).fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Verify Calculator screen elements
        composeTestRule.onNodeWithText("TILE CALCULATOR").assertIsDisplayed()
        composeTestRule.onNodeWithText("FLOOR").assertIsDisplayed()
        composeTestRule.onNodeWithText("WALL").assertIsDisplayed()

        // 2. Input Length and Width
        val lengthNode = composeTestRule.onNodeWithContentDescription("Area length")
        lengthNode.performClick()
        lengthNode.performTextInput("5")

        val widthNode = composeTestRule.onNodeWithContentDescription("Area width")
        widthNode.performClick()
        widthNode.performTextInput("4")

        // 3. Select 60 x 60 cm Preset
        composeTestRule.onNodeWithText("60 × 60 cm").performClick()

        // 4. Select 10% Waste
        composeTestRule.onNodeWithContentDescription("10 percent waste").performScrollTo().performClick()

        // 5. Trigger Calculate
        composeTestRule.onNodeWithText("CALCULATE TILES").performScrollTo().performClick()

        // 6. Verify Result: 62 tiles required
        composeTestRule.onNodeWithContentDescription("62 tiles required").performScrollTo().assertIsDisplayed()

        // 7. Save to History
        composeTestRule.onNodeWithText("SAVE TO HISTORY").performScrollTo().performClick()
        composeTestRule.onNodeWithText("SAVED").performScrollTo().assertIsDisplayed()

        // 8. Navigate to History Tab
        composeTestRule.onNodeWithText("History").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("CALCULATION HISTORY")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("62 tiles").assertIsDisplayed()

        // 9. Navigate to Settings Tab
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("SETTINGS")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("APPEARANCE").assertIsDisplayed()
        composeTestRule.onNodeWithText("CALCULATION DEFAULTS").assertIsDisplayed()

        // 10. Navigate to About Tab
        composeTestRule.onNodeWithText("About").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("ABOUT")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Tile Calculator").assertIsDisplayed()
        composeTestRule.onNodeWithText("Version 1.0 • Professional Offline Utility").assertIsDisplayed()

        // 11. Navigate back to Calculator and Reset
        composeTestRule.onNodeWithText("Calculator").performClick()
        composeTestRule.onNodeWithText("RESET").performScrollTo().performClick()

        // 12. Toggle Wall mode
        composeTestRule.onNodeWithText("WALL").performScrollTo().performClick()
        composeTestRule.onNodeWithText("WALL AREA").assertIsDisplayed()
    }
}
