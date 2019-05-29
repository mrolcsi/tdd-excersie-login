package com.example.tdd.di

import com.example.tdd.login.LoginFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsModule {

  @ContributesAndroidInjector
  abstract fun contributeLoginFragment(): LoginFragment

}