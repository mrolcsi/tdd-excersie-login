package com.example.tdd.login

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.tdd.R
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_login, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // Add TextWatchers to fields
    etUsername?.addTextChangedListener(afterTextChanged = this::onUsernameOrPasswordChanged)
    etPassword?.addTextChangedListener(afterTextChanged = this::onUsernameOrPasswordChanged)
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onUsernameOrPasswordChanged(text: Editable?) {
    val hasUsername = etUsername.text?.isNotBlank() ?: false
    val hasPassword = etPassword.text?.isNotBlank() ?: false
    btnLogin.isEnabled = hasUsername && hasPassword
  }
}