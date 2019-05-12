package com.example.tdd.login

import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tdd.api.AuthenticationApi
import com.example.tdd.api.models.AuthenticationResponse
import org.jetbrains.annotations.TestOnly
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel() : ViewModel() {

  @TestOnly
  constructor(apiUrl: String) : this() {
    mApiUrl = apiUrl
  }

  private var mApiUrl: String = AuthenticationApi.API_URL

  private val mAuthenticationState = MutableLiveData<@AuthenticationState Int>().apply {
    value = UNAUTHENTICATED
  }
  val authenticationState: LiveData<Int>
    get() = mAuthenticationState

  fun login(username: String, password: String) {
    mAuthenticationState.postValue(IN_PROGRESS)
    AuthenticationApi.getInstance(mApiUrl)
      .authenticate(username, password)
      .enqueue(object : Callback<AuthenticationResponse> {
        override fun onFailure(call: Call<AuthenticationResponse>, t: Throwable) {
          mAuthenticationState.postValue(NETWORK_ERROR)
        }

        override fun onResponse(call: Call<AuthenticationResponse>, response: Response<AuthenticationResponse>) {
          when (response.code()) {
            200 -> mAuthenticationState.postValue(AUTHENTICATED)
            401 -> mAuthenticationState.postValue(AUTHENTICATION_FAILED)
            else -> mAuthenticationState.postValue(UNKNOWN_ERROR)
          }
        }
      })
  }

  companion object {
    const val UNKNOWN_ERROR = -2
    const val NETWORK_ERROR = -1
    const val UNAUTHENTICATED = 0
    const val IN_PROGRESS = 1
    const val AUTHENTICATED = 200
    const val AUTHENTICATION_FAILED = 401
  }

  @IntDef(UNAUTHENTICATED, AUTHENTICATED, AUTHENTICATION_FAILED)
  @Retention(AnnotationRetention.SOURCE)
  annotation class AuthenticationState

}