package com.example.tdd.util

import android.content.SharedPreferences

class InMemorySharedPreferences : SharedPreferences, SharedPreferences.Editor {

  private val preferences = HashMap<String, Any?>()

  override fun contains(key: String): Boolean {
    return preferences.contains(key)
  }

  override fun getBoolean(key: String, defValue: Boolean): Boolean {
    return preferences[key] as Boolean? ?: defValue
  }

  override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    TODO("not implemented")
  }

  override fun getInt(key: String, defValue: Int): Int {
    return preferences[key] as Int? ?: defValue
  }

  override fun getAll(): MutableMap<String, *> {
    return preferences
  }

  override fun edit(): SharedPreferences.Editor {
    return this
  }

  override fun getLong(key: String, defValue: Long): Long {
    return preferences[key] as Long? ?: defValue
  }

  override fun getFloat(key: String, defValue: Float): Float {
    return preferences[key] as Float? ?: defValue
  }

  @Suppress("UNCHECKED_CAST")
  override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
    return preferences[key] as MutableSet<String>? ?: defValues
  }

  override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    TODO("not implemented")
  }

  override fun getString(key: String, defValue: String?): String? {
    return preferences[key] as String? ?: defValue
  }

  override fun putLong(key: String, value: Long): SharedPreferences.Editor {
    preferences[key] = value
    return this
  }

  override fun putInt(key: String, value: Int): SharedPreferences.Editor {
    preferences[key] = value
    return this
  }

  override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
    preferences[key] = value
    return this
  }

  override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor {
    preferences[key] = values
    return this
  }

  override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
    preferences[key] = value
    return this
  }

  override fun putString(key: String, value: String?): SharedPreferences.Editor {
    preferences[key] = value
    return this
  }

  override fun remove(key: String): SharedPreferences.Editor {
    preferences.remove(key)
    return this
  }

  override fun clear(): SharedPreferences.Editor {
    preferences.clear()
    return this
  }

  override fun commit(): Boolean {
    return true
  }

  override fun apply() {
  }
}