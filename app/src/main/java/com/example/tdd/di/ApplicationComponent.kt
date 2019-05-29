package com.example.tdd.di

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Component(
  modules = [
    AndroidSupportInjectionModule::class,
    ActivitiesModule::class,
    FragmentsModule::class,
    ViewModelModule::class,
    ContextModule::class,
    NetworkModule::class
  ]
)
@Singleton
interface ApplicationComponent : AndroidInjector<BaseApplication> {

  interface Factory : AndroidInjector.Factory<BaseApplication>
}