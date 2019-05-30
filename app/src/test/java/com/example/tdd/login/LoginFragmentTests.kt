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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.tdd.R
import com.example.tdd.di.InjectingViewModelFactory
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
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
//@Config(application = TestApplication::class)
class LoginFragmentTests {

  private lateinit var scenario: FragmentScenario<TestLoginFragment>

  @Mock private lateinit var mockViewModel: LoginViewModel
  private val fakeAuthenticationState = MutableLiveData<LoginViewModel.AuthenticationState>()
  private val fakeIsLoginEnabled = MediatorLiveData<Boolean>()

  @Mock private lateinit var mockViewModelFactory: InjectingViewModelFactory

  @Mock private lateinit var mockNavController: NavController

  @Before
  fun setUp() {
    // Prepare mocks
    MockitoAnnotations.initMocks(this)

    `when`(mockViewModelFactory.create(LoginViewModel::class.java)).thenReturn(mockViewModel)

    `when`(mockViewModel.authenticationState).thenReturn(fakeAuthenticationState)

    `when`(mockViewModel.isInProgress).thenReturn(
      Transformations.map(fakeAuthenticationState) { state ->
        state == LoginViewModel.AuthenticationState.IN_PROGRESS
      }
    )

    `when`(mockViewModel.isLoginEnabled).thenReturn(fakeIsLoginEnabled)

    // Inject mocked ViewModelFactory
    TestLoginFragment.testViewModelFactory = mockViewModelFactory

    // Launch fragment
    scenario = launchFragmentInContainer<TestLoginFragment>(themeResId = R.style.AppTheme) {
      TestLoginFragment().apply {
        viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
          viewLifecycleOwner?.run {
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

      val textPassword = fragment.view?.findViewById<TextInputLayout>(R.id.textPassword)

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
  fun test_fragment_hasViewModel() {
    scenario.onFragment { fragment ->
      assertNotNull(fragment.viewModel)
      assertThat(fragment.viewModel, IsInstanceOf.instanceOf(LoginViewModel::class.java))
    }
  }

  @Suppress("UNCHECKED_CAST")
  @Test
  fun test_typedTextIsSaved() {

    // Prepare mocks
    val mockUsernameField = mock(MutableLiveData::class.java) as MutableLiveData<String>
    val mockPasswordField = mock(MutableLiveData::class.java) as MutableLiveData<String>

    `when`(mockViewModel.username).thenReturn(mockUsernameField)
    `when`(mockViewModel.password).thenReturn(mockPasswordField)

    val username = "username"
    val password = "password"

    val usernameCaptor = ArgumentCaptor.forClass(String::class.java)
    val passwordCaptor = ArgumentCaptor.forClass(String::class.java)

    scenario.onFragment {

      onView(withId(R.id.etUsername)).perform(typeText(username))
      onView(withId(R.id.etPassword)).perform(typeText(password))

      // Text is typed in character by character
      verify(mockViewModel.username, times(username.length)).value = usernameCaptor.capture()
      verify(mockViewModel.password, times(password.length)).value = passwordCaptor.capture()

      assertEquals(username, usernameCaptor.value)
      assertEquals(password, passwordCaptor.value)
    }

  }

  @Test
  fun test_loginWasCalled_withInputParameters() {
    scenario.onFragment { fragment ->

      // Add some texts into the fields
      val username = "username"
      val password = "password"

      `when`(mockViewModel.username).thenReturn(MutableLiveData(username))
      `when`(mockViewModel.password).thenReturn(MutableLiveData(password))

      // Simulate a click on the button
      //onView(withId(R.id.btnLogin)).perform(click())
      fragment.view?.findViewById<Button>(R.id.btnLogin)?.performClick()

      // Verify arguments
      verify(fragment.viewModel).login(username, password)
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
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.IN_PROGRESS

      onView(withId(R.id.loginProgress)).check { view, _ ->
        assertThat("Progress Bar is not visible!", view.visibility, equalTo(View.VISIBLE))
      }
    }
  }

  @Test
  fun test_whenLoginSucceeds_progressBarIsHidden() {
    scenario.onFragment {

      // Simulate login in progress
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.IN_PROGRESS

      // Simulate success
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.AUTHENTICATED

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
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.IN_PROGRESS

      // Simulate failure
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.AUTHENTICATION_FAILED

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
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.IN_PROGRESS

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
  fun test_whenLoginIsEnabled_buttonIsEnabled() {

    // Enabled

    scenario.onFragment {

      fakeIsLoginEnabled.value = true

      onView(withId(R.id.btnLogin)).check { view, _ ->
        assertTrue("Login Button is not enabled!", view.isEnabled)
      }
    }

    // Disabled

    scenario.onFragment {

      fakeIsLoginEnabled.value = false


      onView(withId(R.id.btnLogin)).check { view, _ ->
        assertFalse("Login Button is enabled!", view.isEnabled)
      }
    }
  }

  @Test
  fun test_whenLoginSucceeds_proceedsToHomeView() {
    scenario.onFragment {

      // Simulate successful login
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.AUTHENTICATED

      // Check if navigation is properly called
      verify(mockNavController).navigate(R.id.navHome)
    }

  }

  @Test
  fun test_whenAuthenticationFails_alertIsVisible() {
    scenario.onFragment {

      // Simulate failure
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.AUTHENTICATION_FAILED

      val dialog = ShadowAlertDialog.getLatestAlertDialog()

      validateDialog(dialog, R.string.login_invalidUsernameOrPassword)
    }
  }

  @Test
  fun test_whenConnectionErrorOccurs_alertIsVisible() {
    scenario.onFragment {

      // Simulate connection error
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.NETWORK_ERROR

      val dialog = ShadowAlertDialog.getLatestAlertDialog()

      validateDialog(dialog, R.string.login_connectionBroken)
    }
  }

  @Test
  fun test_whenOtherErrorOccurs_alertIsVisible() {
    scenario.onFragment {

      // Simulate connection error
      fakeAuthenticationState.value = LoginViewModel.AuthenticationState.UNKNOWN_ERROR

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

  /**
   * https://proandroiddev.com/testing-dagger-fragments-with-fragmentscenario-155b6ad18747
   */
  class TestLoginFragment : LoginFragment() {

    override fun inject() {
      viewModelFactory = testViewModelFactory
    }

    companion object {
      lateinit var testViewModelFactory: ViewModelProvider.Factory
    }
  }
}