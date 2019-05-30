package com.example.tdd.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

  private val _authenticationState = MutableLiveData<AuthenticationState>(AuthenticationState.UNAUTHENTICATED)
  val authenticationState: LiveData<AuthenticationState> = _authenticationState

  init {
    val token = tokenStore.refreshToken
    if (token != null) {
      login(token)
    }
  }

  //region -- DATA BINDING --

  val username = MutableLiveData<String>()
  var password = MutableLiveData<String>()

  val isInProgress: LiveData<Boolean> = Transformations.map(_authenticationState) { state ->
    state == AuthenticationState.IN_PROGRESS
  }

  val isLoginEnabled = MediatorLiveData<Boolean>().apply {
    addSource(username) { value = updateLoginEnabled() }
    addSource(password) { value = updateLoginEnabled() }
    addSource(isInProgress) { value = updateLoginEnabled() }
  }

  private fun updateLoginEnabled(): Boolean {
    return !isInProgress.value!! && !username.value.isNullOrBlank() && !password.value.isNullOrBlank()
  }

  //endregion

  fun login(username: String, password: String) {
    _authenticationState.postValue(AuthenticationState.IN_PROGRESS)
    authenticationService
      .authenticate(username, password)
      .enqueue(this)
  }

  fun login(token: String) {
    _authenticationState.postValue(AuthenticationState.IN_PROGRESS)
    authenticationService
      .extendAuthentication(token)
      .enqueue(this)
  }

  override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
    _authenticationState.postValue(AuthenticationState.NETWORK_ERROR)
  }

  override fun onResponse(call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>) {
    when (response.code()) {
      200 -> {
        response.body()?.run {
          tokenStore.accessToken = accessToken
          tokenStore.refreshToken = refreshToken
        }
        _authenticationState.postValue(AuthenticationState.AUTHENTICATED)
      }
      401 -> _authenticationState.postValue(AuthenticationState.AUTHENTICATION_FAILED)
      else -> _authenticationState.postValue(AuthenticationState.UNKNOWN_ERROR)
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