package com.example.tdd.login

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.tdd.R
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

  internal lateinit var viewModel: LoginViewModel

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    if (!this::viewModel.isInitialized) {
      viewModel = ViewModelProviders.of(this, LoginViewModel.Factory(requireContext()))
        .get(LoginViewModel::class.java)
    }

    viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
      loginProgress.visibility = if (state == LoginViewModel.IN_PROGRESS) View.VISIBLE else View.GONE
      setInputsEnabled(state != LoginViewModel.IN_PROGRESS)

      when (state) {
        LoginViewModel.AUTHENTICATED -> findNavController().navigate(R.id.navHome)
        LoginViewModel.AUTHENTICATION_FAILED -> AlertDialog.Builder(requireContext())
          .setMessage(R.string.login_invalidUsernameOrPassword)
          .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
          .show()
        LoginViewModel.NETWORK_ERROR -> AlertDialog.Builder(requireContext())
          .setMessage(R.string.login_connectionBroken)
          .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
          .show()
        LoginViewModel.UNKNOWN_ERROR -> AlertDialog.Builder(requireContext())
          .setMessage(R.string.login_unexpectedError)
          .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
          .show()
      }
    })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_login, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // Add TextWatchers to fields
    etUsername?.addTextChangedListener(afterTextChanged = this::onUsernameOrPasswordChanged)
    etPassword?.addTextChangedListener(afterTextChanged = this::onUsernameOrPasswordChanged)

    btnLogin?.setOnClickListener {
      viewModel.login(etUsername.text.toString(), etPassword.text.toString())
    }
  }

  private fun isLoginButtonEnabled(): Boolean {
    val hasUsername = etUsername.text?.isNotBlank() ?: false
    val hasPassword = etPassword.text?.isNotBlank() ?: false
    return hasUsername && hasPassword
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onUsernameOrPasswordChanged(text: Editable?) {
    btnLogin.isEnabled = isLoginButtonEnabled()
  }

  private fun setInputsEnabled(isEnabled: Boolean) {
    etUsername.isEnabled = isEnabled
    etPassword.isEnabled = isEnabled

    btnLogin.isEnabled = isEnabled && isLoginButtonEnabled()
  }
}