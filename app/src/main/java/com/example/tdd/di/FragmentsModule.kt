package com.example.tdd.di

import com.example.tdd.login.LoginFragment
import com.example.tdd.login.LoginModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentsModule {

  @ContributesAndroidInjector(modules = [LoginModule::class])
  abstract fun contributeLoginFragment(): LoginFragment

}