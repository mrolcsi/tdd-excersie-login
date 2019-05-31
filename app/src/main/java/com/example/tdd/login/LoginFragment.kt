package com.example.tdd.login

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.tdd.R
import com.example.tdd.databinding.FragmentLoginBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

open class LoginFragment : Fragment() {

  @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
  lateinit var viewModel: LoginViewModel

  private lateinit var binding: FragmentLoginBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    inject()
    super.onCreate(savedInstanceState)
  }

  protected open fun inject() {
    AndroidSupportInjection.inject(this)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel::class.java)
    binding.viewModel = viewModel

    viewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
      when (state) {
        LoginViewModel.AuthenticationState.AUTHENTICATED -> findNavController().navigate(R.id.navHome)
        LoginViewModel.AuthenticationState.AUTHENTICATION_FAILED -> showErrorMessage(R.string.login_invalidUsernameOrPassword)
        LoginViewModel.AuthenticationState.NETWORK_ERROR -> showErrorMessage(R.string.login_connectionBroken)
        LoginViewModel.AuthenticationState.UNKNOWN_ERROR -> showErrorMessage(R.string.login_unexpectedError)
        else -> {
          //nothing
        }
      }
    })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    FragmentLoginBinding.inflate(inflater, container, false).also {
      binding = it
      it.lifecycleOwner = viewLifecycleOwner
    }.root

  private fun showErrorMessage(@StringRes messageId: Int) {
    AlertDialog.Builder(requireContext())
      .setMessage(messageId)
      .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
      .show()
  }
}