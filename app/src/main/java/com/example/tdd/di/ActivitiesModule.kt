package com.example.tdd.di

import com.example.tdd.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {

  @ContributesAndroidInjector   // (modules = [MainActivityModule::class])
  abstract fun contributeMainActivity(): MainActivity

}