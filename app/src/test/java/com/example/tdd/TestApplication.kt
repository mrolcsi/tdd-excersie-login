package com.example.tdd

import android.app.Application

class TestApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    setTheme(R.style.AppTheme)
  }
}