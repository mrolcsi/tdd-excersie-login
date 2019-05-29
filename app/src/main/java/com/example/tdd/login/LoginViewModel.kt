package com.example.tdd.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tdd.api.AuthenticationApi
import com.example.tdd.api.models.AuthenticationResponse
import com.example.tdd.session.TokenStore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class LoginViewModel @Inject constructor(
  private val tokenStore: TokenStore,
  private val authenticationService: AuthenticationApi
) : ViewModel(), Callback<AuthenticationResponse> {

  private val mAuthenticationState = MutableLiveData<AuthenticationState>().apply {
    value = AuthenticationState.UNAUTHENTICATED
  }
  val authenticationState: LiveData<AuthenticationState>
    get() = mAuthenticationState

  init {
    val token = tokenStore.refreshToken
    if (token != null) {
      login(token)
    }
  }

  fun login(username: String, password: String) {
    mAuthenticationState.postValue(AuthenticationState.IN_PROGRESS)
    authenticationService
      .authenticate(username, password)
      .enqueue(this)
  }

  fun login(token: String) {
    mAuthenticationState.postValue(AuthenticationState.IN_PROGRESS)
    authenticationService
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
          tokenStore.accessToken = accessToken
          tokenStore.refreshToken = refreshToken
        }
        mAuthenticationState.postValue(AuthenticationState.AUTHENTICATED)
      }
      401 -> mAuthenticationState.postValue(AuthenticationState.AUTHENTICATION_FAILED)
      else -> mAuthenticationState.postValue(AuthenticationState.UNKNOWN_ERROR)
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