package com.example.tdd.login

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.tdd.R
import com.example.tdd.databinding.FragmentLoginBinding
import dagger.android.support.DaggerFragment
import javax.inject.Inject

open class LoginFragment : DaggerFragment() {

  @Inject lateinit var viewModel: LoginViewModel

  private lateinit var binding: FragmentLoginBinding

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
      when (state) {
        LoginViewModel.AuthenticationState.AUTHENTICATED -> findNavController().navigate(R.id.navHome)
        LoginViewModel.AuthenticationState.AUTHENTICATION_FAILED -> showErrorMessage(R.string.login_invalidUsernameOrPassword)
        LoginViewModel.AuthenticationState.NETWORK_ERROR -> showErrorMessage(R.string.login_connectionBroken)
        LoginViewModel.AuthenticationState.UNKNOWN_ERROR -> showErrorMessage(R.string.login_unexpectedError)
        else -> {
          //nothing to do
        }
      }
    })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    FragmentLoginBinding.inflate(inflater, container, false).also {
      binding = it
      it.lifecycleOwner = viewLifecycleOwner
      it.viewModel = viewModel
    }.root

  private fun showErrorMessage(@StringRes messageId: Int) {
    AlertDialog.Builder(requireContext())
      .setMessage(messageId)
      .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
      .show()
  }
}