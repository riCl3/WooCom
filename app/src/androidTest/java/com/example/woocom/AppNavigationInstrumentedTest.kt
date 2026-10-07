package com.example.woocom

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.woocom.ui.theme.WooComTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appStartsOnSplashScreen() {
        composeRule.setContent {
            WooComTheme {
                AppNavigation()
            }
        }
        composeRule.onNodeWithText("WooCom").assertExists()
    }

    @Test
    fun applicationContextIsWooCom() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.woocom", appContext.packageName)
    }
}
