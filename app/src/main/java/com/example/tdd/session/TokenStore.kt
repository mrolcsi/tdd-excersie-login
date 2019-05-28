package com.example.tdd.session

import android.content.Context
import androidx.core.content.edit

/**
 * See https://medium.com/@tsaha.cse/advanced-retrofit2-part-2-authorization-handling-ea1431cb86be
 */
class TokenStore(context: Context) {

  private val sharedPrefs = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)

  var accessToken: String? = null

  var refreshToken: String?
    get() = sharedPrefs.getString(PREF_REFRESH_TOKEN, null)
    set(value) = sharedPrefs.edit { putString(PREF_REFRESH_TOKEN, value) }

  companion object {
    private const val PREFS_SESSION = "session"
    const val PREF_REFRESH_TOKEN = "refresh_token"
  }
}