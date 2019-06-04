package com.example.tdd.login

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.tdd.api.AuthenticationApi
import com.example.tdd.api.models.AuthenticationResponse
import com.example.tdd.login.LoginViewModel.AuthenticationState
import com.example.tdd.session.TokenStore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class LoginViewModelImpl @Inject constructor(
  private val tokenStore: TokenStore,
  private val authenticationService: AuthenticationApi
) : ViewModel(), LoginViewModel {

  init {
    tokenStore.refreshToken?.let { login(it) }
  }

  private var compositeDisposable: CompositeDisposable = CompositeDisposable()

  override val authenticationState = MutableLiveData<AuthenticationState>(AuthenticationState.UNAUTHENTICATED)

  //region -- DATA BINDING --

  override var username = MutableLiveData<String>()
  override var password = MutableLiveData<String>()

  override val isInProgress = Transformations.map(authenticationState) { state ->
    state == AuthenticationState.IN_PROGRESS
  }

  override val isLoginEnabled = MediatorLiveData<Boolean>().apply {
    addSource(username) { value = updateLoginEnabled() }
    addSource(password) { value = updateLoginEnabled() }
    addSource(isInProgress) { value = updateLoginEnabled() }
  }

  private fun updateLoginEnabled(): Boolean {
    return !isInProgress.value!! && !username.value.isNullOrBlank() && !password.value.isNullOrBlank()
  }

  override fun onLoginClicked() {
    login(username.value!!, password.value!!)
  }

  //endregion

  fun login(username: String, password: String) {
    authenticationState.value = AuthenticationState.IN_PROGRESS
    compositeDisposable.add(
      authenticationService
        .authenticate(username, password)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::onSuccess, this::onFailure)
    )
  }

  fun login(token: String) {
    authenticationState.postValue(AuthenticationState.IN_PROGRESS)
    compositeDisposable.add(
      authenticationService
        .extendAuthentication(token)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::onSuccess, this::onFailure)
    )
  }

  private fun onSuccess(response: AuthenticationResponse) {
    authenticationState.postValue(AuthenticationState.AUTHENTICATED)
    tokenStore.accessToken = response.accessToken
    tokenStore.refreshToken = response.refreshToken
  }

  private fun onFailure(throwable: Throwable) {
    if (throwable is HttpException) {
      when (throwable.code()) {
        401 -> authenticationState.postValue(AuthenticationState.AUTHENTICATION_FAILED)
        else -> authenticationState.postValue(AuthenticationState.UNKNOWN_ERROR)
      }
    } else {
      authenticationState.postValue(AuthenticationState.NETWORK_ERROR)
    }
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

}