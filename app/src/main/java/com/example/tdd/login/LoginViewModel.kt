package com.example.tdd.login

import android.content.Context
import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tdd.api.AuthenticationApi
import com.example.tdd.api.models.AuthenticationResponse
import com.example.tdd.session.TokenStore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(
  private val TokenStore: TokenStore,
  private val apiUrl: String = AuthenticationApi.API_URL
) : ViewModel(), Callback<AuthenticationResponse> {

  private val mAuthenticationState = MutableLiveData<@AuthenticationState Int>().apply {
    value = UNAUTHENTICATED
  }
  val authenticationState: LiveData<Int>
    get() = mAuthenticationState

  init {
    val token = TokenStore.refreshToken
    if (token != null) {
      login(token)
    }
  }

  fun login(username: String, password: String) {
    mAuthenticationState.postValue(IN_PROGRESS)
    AuthenticationApi.getInstance(apiUrl)
      .authenticate(username, password)
      .enqueue(this)
  }

  fun login(token: String) {
    mAuthenticationState.postValue(IN_PROGRESS)
    AuthenticationApi.getInstance(apiUrl)
      .extendAuthentication(token)
      .enqueue(this)
  }

  override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
    mAuthenticationState.postValue(NETWORK_ERROR)
  }

  override fun onResponse(call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>) {
    when (response.code()) {
      200 -> {
        response.body()?.run {
          TokenStore.accessToken = accessToken
          TokenStore.refreshToken = refreshToken
        }
        mAuthenticationState.postValue(AUTHENTICATED)
      }
      401 -> mAuthenticationState.postValue(AUTHENTICATION_FAILED)
      else -> mAuthenticationState.postValue(UNKNOWN_ERROR)
    }
  }

  companion object {
    const val UNKNOWN_ERROR = -2
    const val NETWORK_ERROR = -1
    const val UNAUTHENTICATED = 0
    const val IN_PROGRESS = 1
    const val AUTHENTICATED = 200
    const val AUTHENTICATION_FAILED = 401
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return LoginViewModel(TokenStore(context)) as T
    }
  }

  @IntDef(UNAUTHENTICATED, AUTHENTICATED, AUTHENTICATION_FAILED)
  @Retention(AnnotationRetention.SOURCE)
  annotation class AuthenticationState

}