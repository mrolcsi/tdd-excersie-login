package com.example.tdd.login

import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.tdd.R
import com.example.tdd.TestApplication
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(application = TestApplication::class)
@RunWith(RobolectricTestRunner::class)
class LoginFragmentTests {

  private lateinit var scenario: FragmentScenario<LoginFragment>

  @Before
  fun setUp() {
    scenario = FragmentScenario.launchInContainer(LoginFragment::class.java)
  }

  @Test
  fun test_requiredViews_exist() {
    scenario.onFragment { fragment ->
      Assert.assertNotNull("fragment has no views!", fragment.view)

      fragment.view?.run {
        Assert.assertNotNull(
          "a required view was not found: etUsername",
          findViewById<EditText>(R.id.etUsername)
        )
        Assert.assertNotNull(
          "a required view was not found: etPassword",
          findViewById<EditText>(R.id.etPassword)
        )
        Assert.assertNotNull(
          "a required view was not found: btnLogin",
          findViewById<Button>(R.id.btnLogin)
        )
        Assert.assertNotNull(
          "a required view was not found: loginProgress",
          findViewById<ProgressBar>(R.id.loginProgress)
        )
      }
    }
  }

  @Test
  fun test_showHidePassword_enabled() {
    scenario.onFragment { fragment ->

      val textPassword = fragment.view?.run {
        findViewById<TextInputLayout>(R.id.textPassword)
      }

      Assert.assertNotNull(
        "a required view was not found: textPassword",
        textPassword
      )
      Assert.assertThat(
        "password visibility toggle is not enabled!",
        textPassword!!.isPasswordVisibilityToggleEnabled,
        equalTo(true)
      )
    }
  }

  @Test
  fun test_whenUsernameOrPasswordEmpty_loginDisabled() {
    scenario.onFragment { fragment ->

      fragment.view?.run {
        // Check initial states: fields are empty, button is disabled
        onView(withId(R.id.etUsername)).check { view, _ ->
          Assert.assertTrue("etUsername should be empty!", (view as EditText).text.isEmpty())
        }
        onView(withId(R.id.etPassword)).check { view, _ ->
          Assert.assertTrue("etPassword should be empty!", (view as EditText).text.isEmpty())
        }
        onView(withId(R.id.btnLogin)).check { view, _ ->
          Assert.assertFalse("btnLogin should be disabled", view.isEnabled)
        }

        // Add text to username field -> button is still disabled
        onView(withId(R.id.etUsername)).perform(typeText("testUser"))
        onView(withId(R.id.btnLogin)).check { view, _ ->
          Assert.assertFalse("btnLogin should be disabled", view.isEnabled)
        }

        // Add text to password -> button becomes enabled
        onView(withId(R.id.etPassword)).perform(typeText("testPassword"))
        onView(withId(R.id.btnLogin)).check { view, _ ->
          Assert.assertTrue("btnLogin should be enabled", view.isEnabled)
        }

        // Clear text from username -> button becomes disabled
        onView(withId(R.id.etUsername)).perform(clearText())
        onView(withId(R.id.btnLogin)).check { view, _ ->
          Assert.assertFalse("btnLogin should be disabled", view.isEnabled)
        }

        // Clear text from password -> button is still disabled
        onView(withId(R.id.etPassword)).perform(clearText())
        onView(withId(R.id.btnLogin)).check { view, _ ->
          Assert.assertFalse("btnLogin should be disabled", view.isEnabled)
        }

        // That's a full circle.
      }

    }
  }
}