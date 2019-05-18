package com.example.tdd.login

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.tdd.R
import com.example.tdd.TestApplication
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class LoginFragmentTests {

  private lateinit var scenario: FragmentScenario<LoginFragment>

  @Mock private lateinit var mockViewModel: LoginViewModel
  private val fakeAuthenticationState = MutableLiveData<Int>()

  @Mock private lateinit var mockNavController: NavController

  @Before
  fun setUp() {
    // Prepare mocks
    MockitoAnnotations.initMocks(this)
    `when`(mockViewModel.authenticationState).thenReturn(fakeAuthenticationState)

    // Launch fragment
    scenario = launchFragmentInContainer<LoginFragment> {
      LoginFragment().apply {
        // Inject mock ViewModel into Fragment
        viewModel = mockViewModel
        // Register a mock NavController when view is created
        viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
          if (viewLifecycleOwner != null) {
            // The fragment’s view has just been created
            Navigation.setViewNavController(requireView(), mockNavController)
          }
        }
      }
    }


  }

  @Test
  fun test_requiredViews_exist() {
    scenario.onFragment { fragment ->
      assertNotNull("fragment has no views!", fragment.view)

      fragment.view?.run {
        assertNotNull(
          "a required view was not found: etUsername",
          findViewById<EditText>(R.id.etUsername)
        )
        assertNotNull(
          "a required view was not found: etPassword",
          findViewById<EditText>(R.id.etPassword)
        )
        assertNotNull(
          "a required view was not found: btnLogin",
          findViewById<Button>(R.id.btnLogin)
        )
        assertNotNull(
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

      assertNotNull(
        "a required view was not found: textPassword",
        textPassword
      )
      assertTrue(
        "password visibility toggle is not enabled!",
        textPassword!!.isPasswordVisibilityToggleEnabled
      )
    }
  }

  @Test
  fun test_whenUsernameOrPasswordEmpty_loginDisabled() {
    // Check initial states: fields are empty, button is disabled
    onView(withId(R.id.etUsername)).check { view, _ ->
      assertTrue("etUsername should be empty!", (view as EditText).text.isEmpty())
    }
    onView(withId(R.id.etPassword)).check { view, _ ->
      assertTrue("etPassword should be empty!", (view as EditText).text.isEmpty())
    }
    onView(withId(R.id.btnLogin)).check { view, _ ->
      assertFalse("btnLogin should be disabled", view.isEnabled)
    }

    // Add text to username field -> button is still disabled
    onView(withId(R.id.etUsername)).perform(typeText("testUser"))
    onView(withId(R.id.btnLogin)).check { view, _ ->
      assertFalse("btnLogin should be disabled", view.isEnabled)
    }

    // Add text to password -> button becomes enabled
    onView(withId(R.id.etPassword)).perform(typeText("testPassword"))
    onView(withId(R.id.btnLogin)).check { view, _ ->
      assertTrue("btnLogin should be enabled", view.isEnabled)
    }

    // Clear text from username -> button becomes disabled
    onView(withId(R.id.etUsername)).perform(clearText())
    onView(withId(R.id.btnLogin)).check { view, _ ->
      assertFalse("btnLogin should be disabled", view.isEnabled)
    }

    // Clear text from password -> button is still disabled
    onView(withId(R.id.etPassword)).perform(clearText())
    onView(withId(R.id.btnLogin)).check { view, _ ->
      assertFalse("btnLogin should be disabled", view.isEnabled)
    }

    // That's a full circle.
  }

  @Test
  fun test_fragment_hasViewModel() {
    scenario.onFragment { fragment ->
      assertNotNull(fragment.viewModel)
      assertThat(fragment.viewModel, IsInstanceOf.instanceOf(LoginViewModel::class.java))
    }
  }

  @Test
  fun test_loginButton_hasOnClickListener() {
    onView(withId(R.id.btnLogin)).check { view, _ ->
      assertTrue("btnLogin has no OnClickListener", view.hasOnClickListeners())
    }
  }

  @Test
  fun test_loginButton_callsLogin() {
    scenario.onFragment { fragment ->

      // Simulate a click on the button
      fragment.view?.findViewById<Button>(R.id.btnLogin)?.run {
        performClick()
        // Verify if method was called
        verify(fragment.viewModel).login(anyString(), anyString())
      }
    }
  }

  @Test
  fun test_loginWasCalled_withInputParameters() {
    scenario.onFragment { fragment ->

      // Add some texts into the fields
      val username = "username"
      val password = "password"
      onView(withId(R.id.etUsername)).perform(typeText(username))
      onView(withId(R.id.etPassword)).perform(typeText(password))

      // Simulate a click on the button
      fragment.view?.findViewById<Button>(R.id.btnLogin)?.run {
        performClick()
        // Verify arguments
        verify(fragment.viewModel).login(username, password)
      }
    }
  }

  @Test
  fun test_authenticationState_isMocked() {
    // Test of a test...
    scenario.onFragment { fragment ->
      assertEquals(
        "authenticationState is not using mock",
        fragment.viewModel.authenticationState,
        fakeAuthenticationState
      )
    }
  }

  @Test
  fun test_fragment_observesAuthenticationState() {
    scenario.onFragment { fragment ->
      assertTrue(
        "authenticationSate is not observed!",
        fragment.viewModel.authenticationState.hasObservers()
      )
    }
  }

  @Test
  fun test_whenLoginIsInProgress_progressBarIsVisible() {
    // And not visible otherwise

    scenario.onFragment {
      // Check if progress bar is hidden by default
      onView(withId(R.id.loginProgress)).check { view, _ ->
        assertThat("Progress Bar is not hidden!", view.visibility, equalTo(View.GONE))
      }

      // Simulate login in progress
      fakeAuthenticationState.value = LoginViewModel.IN_PROGRESS

      onView(withId(R.id.loginProgress)).check { view, _ ->
        assertThat("Progress Bar is not visible!", view.visibility, equalTo(View.VISIBLE))
      }
    }
  }

  @Test
  fun test_whenLoginSucceeds_progressBarIsHidden() {
    scenario.onFragment {

      // Simulate login in progress
      fakeAuthenticationState.value = LoginViewModel.IN_PROGRESS

      // Simulate success
      fakeAuthenticationState.value = LoginViewModel.AUTHENTICATED

      // Progress Bar should be hidden again
      onView(withId(R.id.loginProgress)).check { view, _ ->
        assertThat("Progress Bar is not hidden!", view.visibility, equalTo(View.GONE))
      }
    }
  }

  @Test
  fun test_whenLoginFails_progressBarIsHidden() {
    scenario.onFragment {

      // Simulate login in progress
      fakeAuthenticationState.value = LoginViewModel.IN_PROGRESS

      // Simulate failure
      fakeAuthenticationState.value = LoginViewModel.AUTHENTICATION_FAILED

      // Progress Bar should be hidden again
      onView(withId(R.id.loginProgress)).check { view, _ ->
        assertThat("Progress Bar is not hidden!", view.visibility, equalTo(View.GONE))
      }
    }
  }

  @Test
  fun test_whenLoginIsInProgress_inputsAreDisabled() {
    scenario.onFragment {

      // Simulate login in progress
      fakeAuthenticationState.value = LoginViewModel.IN_PROGRESS

      onView(withId(R.id.etUsername)).check { view, _ ->
        assertFalse("Username Field is not disabled!", view.isEnabled)
      }
      onView(withId(R.id.etPassword)).check { view, _ ->
        assertFalse("Password Field is not disabled!", view.isEnabled)
      }
      onView(withId(R.id.btnLogin)).check { view, _ ->
        assertFalse("Login Button is not disabled!", view.isEnabled)
      }
    }
  }

  @Test
  fun test_whenLoginFails_inputsAreEnabled() {
    scenario.onFragment {

      // Put texts into input fields
      onView(withId(R.id.etUsername)).perform(typeText("username"))
      onView(withId(R.id.etPassword)).perform(typeText("password"))

      // Simulate failure
      fakeAuthenticationState.value = LoginViewModel.AUTHENTICATION_FAILED

      onView(withId(R.id.etUsername)).check { view, _ ->
        assertTrue("Username Field is not enabled!", view.isEnabled)
      }
      onView(withId(R.id.etPassword)).check { view, _ ->
        assertTrue("Password Field is not enabled!", view.isEnabled)
      }
      // Login button is only enabled when there is text in the input fields
      onView(withId(R.id.btnLogin)).check { view, _ ->
        assertTrue("Login Button is not enabled!", view.isEnabled)
      }
    }
  }

  @Test
  fun test_whenLoginSucceeds_proceedsToHomeView() {
    scenario.onFragment {

      // Simulate successful login
      fakeAuthenticationState.value = LoginViewModel.AUTHENTICATED

      // Check if navigation is properly called
      verify(mockNavController).navigate(R.id.navHome)
    }

  }

  @Test
  fun test_whenAuthenticationFails_alertIsVisible() {
    scenario.onFragment {

      // Simulate failure
      fakeAuthenticationState.value = LoginViewModel.AUTHENTICATION_FAILED

      val dialog = ShadowAlertDialog.getLatestAlertDialog()

      validateDialog(dialog, R.string.login_invalidUsernameOrPassword)
    }
  }

  @Test
  fun test_whenConnectionErrorOccurs_alertIsVisible() {
    scenario.onFragment {

      // Simulate connection error
      fakeAuthenticationState.value = LoginViewModel.NETWORK_ERROR

      val dialog = ShadowAlertDialog.getLatestAlertDialog()

      validateDialog(dialog, R.string.login_connectionBroken)
    }
  }

  @Test
  fun test_whenOtherErrorOccurs_alertIsVisible() {
    scenario.onFragment {

      // Simulate connection error
      fakeAuthenticationState.value = LoginViewModel.UNKNOWN_ERROR

      val dialog = ShadowAlertDialog.getLatestAlertDialog()

      validateDialog(dialog, R.string.login_unexpectedError)
    }
  }

  private fun validateDialog(dialog: AlertDialog, @StringRes messageId: Int) {
    // Check if dialog with error message is visible
    assertNotNull("No dialog is present!", dialog)
    assertTrue("No dialogs are shown!", dialog.isShowing)

    val context = ApplicationProvider.getApplicationContext<Context>()

    // Check if OK is visible...
    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    assertEquals(
      "Dialog has no OK button!",
      View.VISIBLE,
      positiveButton.visibility
    )

    // ...and it actually is an OK button.
    assertEquals(
      "OK button has wrong text!",
      context.getString(android.R.string.ok),
      positiveButton.text
    )

    Shadows.shadowOf(dialog).run {
      // Check if dialog shows proper message
      assertEquals(
        "Dialog message is wrong!",
        context.getString(messageId),
        message
      )

      // Press the OK button
      clickOn(positiveButton.id)

      // Check if dialog is dismissed when OK clicked
      assertTrue("Dialog is still shown!", hasBeenDismissed())
    }
  }
}