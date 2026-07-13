package com.akari.app

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * End-to-end smoke walk on an emulator (CI). A fresh install starts empty, so
 * the test completes onboarding before walking every tab, logging an activity
 * (lantern must dim), and entering/exiting crash mode. Saves screenshots to filesDir/screenshots
 * — the CI workflow pulls them out as an artifact.
 *
 * Run with animations disabled (CI does this); the app's reduce-motion path
 * then disables breathe/flicker, which also keeps Compose idle-detection happy.
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String, timeoutMs: Long = 30_000) {
        rule.waitUntil(timeoutMillis = timeoutMs) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun shot(name: String) {
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap()
        // With -Pandroid.enableAdditionalTestOutput=true AGP injects this dir
        // and pulls it to build/outputs/connected_android_test_additional_output
        // after the run (survives the post-test uninstall). Fallback: filesDir.
        val out = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val dir = (out?.let { File(it) } ?: File(rule.activity.filesDir, "screenshots")).apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test
    fun walkTheWholeApp() {
        // Fresh install → onboarding → morning intention → Home.
        waitForText("Skip introduction")
        rule.onNodeWithText("Skip introduction").performClick()
        waitForText("Light the lantern")
        rule.onNodeWithText("Light the lantern").performClick()
        waitForText("SPENT TODAY")
        shot("01-home")

        // ---- tabs ----
        rule.onNodeWithText("Trends").performClick()
        waitForText("LAST 14 DAYS")
        shot("02-trends")

        rule.onNodeWithText("History").performClick()
        waitForText("YOUR DIARY")
        shot("03-history")

        rule.onNodeWithText("Settings").performClick()
        waitForText("YOU & YOUR DATA")
        shot("04-settings")

        rule.onNodeWithText("Today").performClick()
        waitForText("SPENT TODAY")

        // ---- log an activity; the lantern must dim ----
        // A new day starts at 60; "Cook a meal" costs 8 → 52.
        rule.onNodeWithContentDescription("Log something").performClick()
        waitForText("Activity")
        rule.onNodeWithText("Activity").performClick()
        waitForText("Cook a meal")
        shot("05-log-activity")
        rule.onNodeWithText("Cook a meal").performClick()
        waitForText("52")
        shot("06-home-after-log")

        // ---- crash mode: silent confirmation, then back to light ----
        rule.onNodeWithContentDescription("Enter crash mode").performClick()
        waitForText("You're in a crash.")
        shot("07-crash")
        rule.onNodeWithText("I'm resting").performClick()
        waitForText("Resting ✓")
        shot("08-crash-done")
        rule.onNodeWithText("Back to light").performClick()
        waitForText("SPENT TODAY")
    }
}
