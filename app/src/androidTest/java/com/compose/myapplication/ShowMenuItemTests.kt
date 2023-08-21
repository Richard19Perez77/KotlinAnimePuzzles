package com.compose.myapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


/**
 * View tests for puzzle on default non solved puzzle
 *
 * @constructor Create empty View tests
 */
class ShowMenuItemTests {

    private lateinit var mIdlingResource: CountingIdlingResource

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        // Initialize the IdlingResource
        mIdlingResource = CountingIdlingResource("MyIdlingResource")

        // Register the IdlingResource with Espresso
        IdlingRegistry.getInstance().register(mIdlingResource)
    }

    @Test
    fun testMusicToggleVisibility() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(
            R.string.music_toggle
        )).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )
    }

    @Test
    fun testBorderToggleVisibility() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(
            R.string.border_toggle
        )).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )
    }

    @Test
    fun testSoundToggleVisibility() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(
            R.string.set_toggle
        )).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )
    }

    @Test
    fun testWinToggleVisibility() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(
            R.string.win_toggle
        )).check(
            matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            )
        )
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
    }
}