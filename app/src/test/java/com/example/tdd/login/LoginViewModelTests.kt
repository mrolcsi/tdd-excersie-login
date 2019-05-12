package com.example.tdd.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.jraska.livedata.TestObserver
import com.jraska.livedata.test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

// Doesn't need Robolectric
class LoginViewModelTests {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var model: LoginViewModel
  private lateinit var stateObserver: TestObserver<Int>


  @Before
  fun setUp() {
    // Prepare model and observer
    model = LoginViewModel(server.url("/").toString())
    stateObserver = model.authenticationState.test()
  }

  @Test
  fun test_viewModel_exposesState() {
    assertNotNull("authenticationState is null!", model.authenticationState)

    // Check initial state
    assertEquals(LoginViewModel.UNAUTHENTICATED, model.authenticationState.value)
  }

  @Test
  fun testLogin_withUsernamePassword_success() {

    // Mock response
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(AUTHENTICATION_RESPONSE)
    )

    // Request login
    model.login("goodUser", "goodPassword")

    // Login should be successful
    stateObserver
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.AUTHENTICATED
      )
  }

  @Test
  fun testLogin_withUsernamePassword_failed() {

    // Mock response
    server.enqueue(MockResponse().setResponseCode(401))

    // Request login
    model.login("badUser", "badPassword")

    // Login should fail
    stateObserver
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.AUTHENTICATION_FAILED
      )
  }

  @Test
  fun testLogin_withUsernamePassword_networkError() {

    // Simulate network error
    server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

    // Request login
    model.login("testUser", "testPassword")

    // Login should fail
    stateObserver
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.NETWORK_ERROR
      )
  }

  @Test
  fun testLogin_withUsernamePassword_otherError() {
    // For example an unknown response code

    // Simulate an unknown server error
    server.enqueue(MockResponse().setResponseCode(500))

    // Request login
    model.login("testUser", "testPassword")

    // Login should fail
    stateObserver
      .awaitNextValue(10, TimeUnit.SECONDS)
      .assertValueHistory(
        LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.UNKNOWN_ERROR
      )
  }

  companion object {
    private const val AUTHENTICATION_RESPONSE = "{" +
        "   \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZHA6dXNlcl9pZCI6IjUwYTdkYTFkLWZlMDctNGMxNC04YjFiLTAwNzczN2Y0Nzc2MyIsImlkcDp1c2VyX25hbWUiOiJqZG9lIiwiaWRwOmZ1bGxuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiZWRpdG9yIiwiZXhwIjoxNTU2NDc2MjU1fQ.iqFmotBtfAYLplfpLVh_kPgvOIPyV7UMm-NZA06XA5I\"," +
        "   \"token_type\": \"bearer\"," +
        "   \"expires_in\": 119," +
        "   \"refresh_token\": \"NTBhN2RhMWQtZmUwNy00YzE0LThiMWItMDA3NzM3ZjQ3NzYzIyNkNmQ5OTViZS1jY2IxLTQ0MGUtODM4NS1lOTkwMTEwMzBhYzA=\"" +
        "}"

    private lateinit var server: MockWebServer

    @JvmStatic
    @BeforeClass
    fun setUpServer() {
      // Start server
      server = MockWebServer().apply { start() }
    }

    @JvmStatic
    @AfterClass
    fun tearDownServer() {
      // Stop server
      server.shutdown()
    }
  }
}