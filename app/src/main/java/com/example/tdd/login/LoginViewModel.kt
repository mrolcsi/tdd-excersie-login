package com.example.tdd.login

import android.content.Context
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

  private val mAuthenticationState = MutableLiveData<AuthenticationState>().apply {
    value = AuthenticationState.UNAUTHENTICATED
  }
  val authenticationState: LiveData<AuthenticationState>
    get() = mAuthenticationState

  init {
    val token = TokenStore.refreshToken
    if (token != null) {
      login(token)
    }
  }

  fun login(username: String, password: String) {
    mAuthenticationState.postValue(AuthenticationState.IN_PROGRESS)
    AuthenticationApi.getInstance(apiUrl)
      .authenticate(username, password)
      .enqueue(this)
  }

  fun login(token: String) {
    mAuthenticationState.postValue(AuthenticationState.IN_PROGRESS)
    AuthenticationApi.getInstance(apiUrl)
      .extendAuthentication(token)
      .enqueue(this)
  }

  override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
    mAuthenticationState.postValue(AuthenticationState.NETWORK_ERROR)
  }

  override fun onResponse(call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>) {
    when (response.code()) {
      200 -> {
        response.body()?.run {
          TokenStore.accessToken = accessToken
          TokenStore.refreshToken = refreshToken
        }
        mAuthenticationState.postValue(AuthenticationState.AUTHENTICATED)
      }
      401 -> mAuthenticationState.postValue(AuthenticationState.AUTHENTICATION_FAILED)
      else -> mAuthenticationState.postValue(AuthenticationState.UNKNOWN_ERROR)
    }
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
      return LoginViewModel(TokenStore(context)) as T
    }
  }

  enum class AuthenticationState {
    UNKNOWN_ERROR,
    NETWORK_ERROR,
    UNAUTHENTICATED,
    IN_PROGRESS,
    AUTHENTICATED,
    AUTHENTICATION_FAILED,
  }

}