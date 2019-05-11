package com.example.tdd

import android.view.View
import androidx.test.core.app.ActivityScenario
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTests {

  private lateinit var scenario: ActivityScenario<MainActivity>

  @Before
  fun setUp() {
    // AKA test_activityStarts()
    // Start activity
    try {
      scenario = ActivityScenario.launch(MainActivity::class.java)
      scenario.onActivity { activity ->
        // Check if activity has started
        Assert.assertNotNull(activity)
      }
    } catch (e: NullPointerException) {
      assert(false) { "NullPointerException: activity not registered in manifest?" }
    }
  }

  @Test
  fun test_activityContainsNavHost() {
    scenario.onActivity { activity ->
      val navHost = activity.findViewById<View>(R.id.mainNavHost)
      Assert.assertNotNull(navHost)
    }
  }
}