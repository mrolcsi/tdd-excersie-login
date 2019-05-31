package com.example.tdd.session

import android.content.Context
import android.content.SharedPreferences
import com.example.tdd.api.models.AuthenticationResponse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class SharedTokenStoreTests {

  private lateinit var tokenStore: TokenStore
  private lateinit var fakeToken: AuthenticationResponse

  @Mock private lateinit var mockPreferences: SharedPreferences
  @Mock private lateinit var mockEditor: SharedPreferences.Editor

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    `when`(mockPreferences.edit()).thenReturn(mockEditor)

    val mockContext = mock(Context::class.java)
    `when`(
      mockContext.getSharedPreferences(
        "session",
        Context.MODE_PRIVATE
      )
    ).thenReturn(mockPreferences)

    tokenStore = SharedPrefsTokenStore(mockContext)

    fakeToken = AuthenticationResponse(
      DUMMY_ACCESS_TOKEN,
      "bearer",
      119,
      DUMMY_REFRESH_TOKEN
    )
  }

  @Test
  fun test_storesAccessToken() {
    // Stored in-memory

    tokenStore.accessToken = DUMMY_ACCESS_TOKEN

    val storedToken = tokenStore.accessToken

    assertEquals("Stored token is different!", DUMMY_ACCESS_TOKEN, storedToken)
  }

  @Test
  fun test_storesRefreshToken() {
    // Stored in shared preferences

    val tokenCaptor = ArgumentCaptor.forClass(String::class.java)

    tokenStore.refreshToken = DUMMY_REFRESH_TOKEN

    verify(mockEditor).putString(eq(SharedPrefsTokenStore.PREF_REFRESH_TOKEN), tokenCaptor.capture())

    assertEquals("Stored token is different!", DUMMY_REFRESH_TOKEN, tokenCaptor.value)
  }

  companion object {
    private const val DUMMY_ACCESS_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZHA6dXNlcl9pZCI6IjUwYTdkYTFkLWZlMDctNGMxNC04YjFiLTAwNzczN2Y0Nzc2MyIsImlkcDp1c2VyX25hbWUiOiJqZG9lIiwiaWRwOmZ1bGxuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiZWRpdG9yIiwiZXhwIjoxNTU2NDc2MjU1fQ.iqFmotBtfAYLplfpLVh_kPgvOIPyV7UMm-NZA06XA5I"
    private const val DUMMY_REFRESH_TOKEN =
      "NTBhN2RhMWQtZmUwNy00YzE0LThiMWItMDA3NzM3ZjQ3NzYzIyNkNmQ5OTViZS1jY2IxLTQ0MGUtODM4NS1lOTkwMTEwMzBhYzA="
  }

}