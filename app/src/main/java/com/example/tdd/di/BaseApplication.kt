package com.example.tdd.di

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

open class BaseApplication : DaggerApplication() {

  override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
    return DaggerApplicationComponent.builder()
      .contextModule(ContextModule(this.applicationContext))
      .build()
  }

}