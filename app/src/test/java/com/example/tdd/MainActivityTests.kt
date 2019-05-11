package com.example.tdd

import android.view.View
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import org.hamcrest.Matchers
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
      Assert.fail("NullPointerException: activity not registered in manifest?")
    }
  }

  @Test
  fun test_activityContainsNavHost() {
    scenario.onActivity { activity ->
      val navHost = activity.findViewById<View>(R.id.mainNavHost)
      Assert.assertNotNull(navHost)
    }
  }

  @Test
  fun test_navigationIsPresent() {
    scenario.onActivity { activity ->
      val navController = activity.findNavController(R.id.mainNavHost)

      Assert.assertNotNull(navController)
      Assert.assertNotNull(navController.graph)
    }
  }

  @Test
  fun test_startDestination() {
    scenario.onActivity { activity ->
      val navController = activity.findNavController(R.id.mainNavHost)

      Assert.assertThat(navController.graph.startDestination, Matchers.equalTo(R.id.navLogin))
    }
  }
}