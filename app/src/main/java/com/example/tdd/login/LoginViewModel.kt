package com.example.tdd.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface LoginViewModel {

  val authenticationState: LiveData<AuthenticationState>

  val username: MutableLiveData<String>
  val password: MutableLiveData<String>

  val isInProgress: LiveData<Boolean>

  val isLoginEnabled: LiveData<Boolean>

  fun onLoginClicked()

  enum class AuthenticationState {
    UNKNOWN_ERROR,
    NETWORK_ERROR,
    UNAUTHENTICATED,
    IN_PROGRESS,
    AUTHENTICATED,
    AUTHENTICATION_FAILED,
  }

}