package com.compose.myapplication

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test

/**
 * View tests for puzzle on default non solved puzzle
 *
 * @constructor Create empty View tests
 */
class DefaultViewTests {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testPuzzleViewVisibility() {
        onView(withId(R.id.puzzle))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testToolbarVisibility() {
        onView(withId(R.id.toolbar))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testMenuItemVisibility() {
        onView(withId(R.id.menu_stats))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testNewPuzzleVisibility() {
        onView(withId(R.id.new_puzzle))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    @Test
    fun testFragmentLabel() {
        onView(withId(R.id.main_coordinator)).check(matches(hasDescendant(withText("First Fragment"))))
    }
}