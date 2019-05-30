package com.example.tdd.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.tdd.api.AuthenticationApi
import com.example.tdd.api.models.AuthenticationResponse
import com.example.tdd.session.TokenStore
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
  private val tokenStore: TokenStore,
  private val authenticationService: AuthenticationApi
) : ViewModel() {

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

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
    compositeDisposable.add(
      authenticationService
        .authenticate(username, password)
        .subscribeOn(Schedulers.io())
        .subscribe(this::onSuccess, this::onFailure)
    )
  }

  fun login(token: String) {
    _authenticationState.postValue(AuthenticationState.IN_PROGRESS)
    compositeDisposable.add(
      authenticationService
        .extendAuthentication(token)
        .subscribeOn(Schedulers.io())
        .subscribe(this::onSuccess, this::onFailure)
    )
  }

  private fun onSuccess(response: AuthenticationResponse) {
    _authenticationState.postValue(AuthenticationState.AUTHENTICATED)
    tokenStore.accessToken = response.accessToken
    tokenStore.refreshToken = response.refreshToken
  }

  private fun onFailure(throwable: Throwable) {
    if (throwable is HttpException) {
      when (throwable.code()) {
        401 -> _authenticationState.postValue(AuthenticationState.AUTHENTICATION_FAILED)
        else -> _authenticationState.postValue(AuthenticationState.UNKNOWN_ERROR)
      }
    } else {
      _authenticationState.postValue(AuthenticationState.NETWORK_ERROR)
    }
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
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