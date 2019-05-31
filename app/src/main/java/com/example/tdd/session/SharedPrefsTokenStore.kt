package com.example.tdd.session

import android.content.Context
import androidx.core.content.edit
import javax.inject.Inject

/**
 * See https://medium.com/@tsaha.cse/advanced-retrofit2-part-2-authorization-handling-ea1431cb86be
 */
class SharedPrefsTokenStore @Inject constructor(context: Context) : TokenStore {

  private val sharedPrefs = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)

  override var accessToken: String? = null

  override var refreshToken: String?
    get() = sharedPrefs.getString(PREF_REFRESH_TOKEN, null)
    set(value) = sharedPrefs.edit { putString(PREF_REFRESH_TOKEN, value) }

  companion object {
    private const val PREFS_SESSION = "session"
    const val PREF_REFRESH_TOKEN = "refresh_token"
  }
}