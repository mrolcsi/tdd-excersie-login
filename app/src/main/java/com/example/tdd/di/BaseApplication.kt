package com.example.tdd.di

import android.app.Activity
import android.app.Application
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

open class BaseApplication : Application(), HasActivityInjector, HasSupportFragmentInjector {

  @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

  @Inject lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>

  override fun activityInjector(): AndroidInjector<Activity> = dispatchingActivityInjector

  override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingFragmentInjector

  lateinit var applicationComponent: ApplicationComponent

  override fun onCreate() {
    super.onCreate()
    applicationComponent = createAppComponent()
    applicationComponent.inject(this)
  }

  protected open fun createAppComponent(): ApplicationComponent =
    DaggerApplicationComponent.builder()
      .contextModule(ContextModule(this.applicationContext))
      .networkModule(NetworkModule())
      .build()
}