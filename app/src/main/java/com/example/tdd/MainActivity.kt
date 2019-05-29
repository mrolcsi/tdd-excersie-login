package com.example.tdd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)

    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
  }
}