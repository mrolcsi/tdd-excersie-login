package com.example.tdd.session

import android.content.Context
import com.example.tdd.api.models.AuthenticationResponse
import com.example.tdd.util.InMemorySharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class SessionManagerTests {

  private lateinit var manager: SessionManager
  private lateinit var fakeToken: AuthenticationResponse

  @Before
  fun setUp() {
    val mockContext = mock(Context::class.java)
    `when`(
      mockContext.getSharedPreferences(
        anyString(),
        eq(Context.MODE_PRIVATE)
      )
    ).thenReturn(InMemorySharedPreferences())

    manager = SessionManager(mockContext)

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

    manager.accessToken = DUMMY_ACCESS_TOKEN

    val storedToken = manager.accessToken

    assertEquals("Stored token is different!", DUMMY_ACCESS_TOKEN, storedToken)
  }

  @Test
  fun test_storesRefreshToken() {
    // Stored in shared preferences

    manager.refreshToken = DUMMY_REFRESH_TOKEN

    val storedToken = manager.refreshToken

    assertEquals("Stored token is different!", DUMMY_REFRESH_TOKEN, storedToken)
  }

  companion object {
    private const val DUMMY_ACCESS_TOKEN =
      "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZHA6dXNlcl9pZCI6IjUwYTdkYTFkLWZlMDctNGMxNC04YjFiLTAwNzczN2Y0Nzc2MyIsImlkcDp1c2VyX25hbWUiOiJqZG9lIiwiaWRwOmZ1bGxuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiZWRpdG9yIiwiZXhwIjoxNTU2NDc2MjU1fQ.iqFmotBtfAYLplfpLVh_kPgvOIPyV7UMm-NZA06XA5I"
    private const val DUMMY_REFRESH_TOKEN =
      "NTBhN2RhMWQtZmUwNy00YzE0LThiMWItMDA3NzM3ZjQ3NzYzIyNkNmQ5OTViZS1jY2IxLTQ0MGUtODM4NS1lOTkwMTEwMzBhYzA="
  }

}