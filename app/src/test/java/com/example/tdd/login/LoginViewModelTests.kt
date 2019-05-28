package com.example.tdd.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.tdd.session.TokenStore
import com.jraska.livedata.test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

// Doesn't need Robolectric
class LoginViewModelTests {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private lateinit var server: MockWebServer

  @Mock private lateinit var mockTokenStore: TokenStore

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    // Start server
    server = MockWebServer().apply { start() }
  }

  @After
  fun tearDown() {
    // Stop server
    server.shutdown()
  }

  private fun createModel(): LoginViewModel {
    return LoginViewModel(mockTokenStore, server.url("/").toString())
  }

  @Test
  fun test_viewModel_exposesState() {
    val model = createModel()

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

    val model = createModel()

    // Request login
    model.login("goodUser", "goodPassword")

    // Login should be successful
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.AUTHENTICATED
      )
  }

  @Test
  fun testLogin_withUsernamePassword_failed() {

    // Mock response
    server.enqueue(MockResponse().setResponseCode(401))

    val model = createModel()

    // Request login
    model.login("badUser", "badPassword")

    // Login should fail
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.AUTHENTICATION_FAILED
      )
  }

  @Test
  fun testLogin_withUsernamePassword_networkError() {

    // Simulate network error
    server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

    val model = createModel()

    // Request login
    model.login("testUser", "testPassword")

    // Login should fail
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.NETWORK_ERROR
      )
  }

  @Test
  fun testLogin_withUsernamePassword_otherError() {
    // For example an unknown response code

    // Simulate an unknown server error
    server.enqueue(MockResponse().setResponseCode(500))

    val model = createModel()

    // Request login
    model.login("testUser", "testPassword")

    // Login should fail
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS)
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.UNKNOWN_ERROR
      )
  }

  @Test
  fun testLogin_withRefreshToken_success() {
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(AUTHENTICATION_RESPONSE)
    )

    val model = createModel()

    model.login("goodToken")

    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS)
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.AUTHENTICATED
      )
  }

  @Test
  fun testLogin_withRefreshToken_failed() {

    // Mock response
    server.enqueue(MockResponse().setResponseCode(401))

    val model = createModel()

    // Request login
    model.login("badToken")

    // Login should fail
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.AUTHENTICATION_FAILED
      )
  }

  @Test
  fun testLogin_withRefreshToken_networkError() {

    // Simulate network error
    server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

    val model = createModel()

    // Request login
    model.login("badToken")

    // Login should fail
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS) // Wait for network call to finish
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.NETWORK_ERROR
      )
  }

  @Test
  fun testLogin_withRefreshToken_otherError() {
    // For example an unknown response code

    // Simulate an unknown server error
    server.enqueue(MockResponse().setResponseCode(500))

    val model = createModel()

    // Request login
    model.login("testToken")

    // Login should fail
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS)
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.UNKNOWN_ERROR
      )
  }

  @Test
  fun testLogin_whenInitialized() {
    // Prepare mocks
    `when`(mockTokenStore.refreshToken).thenReturn("dummyToken")

    // Mock response
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(AUTHENTICATION_RESPONSE)
    )

    // We need a separate models for this
    val model = createModel()

    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS)
      .assertValueHistory(
        //LoginViewModel.UNAUTHENTICATED,
        LoginViewModel.IN_PROGRESS,
        LoginViewModel.AUTHENTICATED
      )
  }

  @Test
  fun test_whenAuthenticated_savesTokens() {
    // Mock response
    server.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(AUTHENTICATION_RESPONSE)
    )

    // Create new model
    val model = createModel()

    model.login("username", "password")

    // Wait for authentication
    model.authenticationState.test()
      .awaitNextValue(10, TimeUnit.SECONDS)
      .assertValue {
        if (it == LoginViewModel.AUTHENTICATED) {
          val tokenCaptor = ArgumentCaptor.forClass(String::class.java)

          verify(mockTokenStore).accessToken = tokenCaptor.capture()
          assertEquals(DUMMY_ACCESS_TOKEN, tokenCaptor.value)

          verify(mockTokenStore).refreshToken = tokenCaptor.capture()
          assertEquals(DUMMY_REFRESH_TOKEN, tokenCaptor.value)
        }
        true
      }
  }

  companion object {
    private const val DUMMY_ACCESS_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZHA6dXNlcl9pZCI6IjUwYTdkYTFkLWZlMDctNGMxNC04YjFiLTAwNzczN2Y0Nzc2MyIsImlkcDp1c2VyX25hbWUiOiJqZG9lIiwiaWRwOmZ1bGxuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiZWRpdG9yIiwiZXhwIjoxNTU2NDc2MjU1fQ.iqFmotBtfAYLplfpLVh_kPgvOIPyV7UMm-NZA06XA5I"
    private const val DUMMY_REFRESH_TOKEN =
      "NTBhN2RhMWQtZmUwNy00YzE0LThiMWItMDA3NzM3ZjQ3NzYzIyNkNmQ5OTViZS1jY2IxLTQ0MGUtODM4NS1lOTkwMTEwMzBhYzA="

    private const val AUTHENTICATION_RESPONSE = "{" +
        "   \"access_token\": \"$DUMMY_ACCESS_TOKEN\"," +
        "   \"token_type\": \"bearer\"," +
        "   \"expires_in\": 119," +
        "   \"refresh_token\": \"$DUMMY_REFRESH_TOKEN\"" +
        "}"
  }
}