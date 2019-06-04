package com.example.tdd.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.tdd.session.SharedPrefsTokenStore
import com.example.tdd.session.TokenStore
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module
class LoginModule {

  @Suppress("UNCHECKED_CAST")
  @Provides
  fun provideLoginViewModel(fragment: LoginFragment, provider: Provider<LoginViewModelImpl>): LoginViewModel {
    return ViewModelProviders.of(fragment, object : ViewModelProvider.Factory {
      override fun <T : ViewModel?> create(modelClass: Class<T>): T = provider.get() as T
    }).get(LoginViewModelImpl::class.java)
  }

  @Provides
  fun provideTokenStore(context: Context): TokenStore = SharedPrefsTokenStore(context)

}