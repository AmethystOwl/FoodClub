package com.example.foodclub.forgot_password

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.foodclub.R
import com.example.foodclub.databinding.FragmentForgotPasswordBinding
import com.example.foodclub.utils.DataState
import com.example.foodclub.utils.hideSoftKeyboard
import com.example.foodclub.utils.showSnack
import com.example.foodclub.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {
    private val viewModel: ForgotPasswordViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.resetPasswordButton.setOnClickListener {
            val emailText = binding.emailTextInputEditText.text
            if (emailText.isNullOrEmpty() || emailText.isBlank()) {
                binding.emailTextInputLayout.error = getString(R.string.email_blank)
                binding.emailTextInputLayout.requestFocus()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText.toString())
                    .matches()
            ) {
                binding.emailTextInputLayout.error = getString(R.string.email_malformatted)
                binding.emailTextInputLayout.requestFocus()
            } else {
                binding.emailTextInputLayout.error = null
                hideSoftKeyboard()
                viewModel.sendResetEmail(emailText.toString())
            }
        }
        viewModel.resetState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    binding.resetPasswordButton.isEnabled = false
                }
                is DataState.Success -> {
                    viewModel.onDoneObservingResetState()
                    binding.resetPasswordButton.isEnabled = true
                    showToast(getString(R.string.reset_email_sent), true)

                }
                is DataState.Error -> {
                    viewModel.onDoneObservingResetState()
                    binding.resetPasswordButton.isEnabled = true
                    showSnack(it.exception.localizedMessage!!, true)
                    Log.d("ForgotPasswordFragment", "sendResetEmail: ${it.exception}")
                }
                else -> {
                    binding.resetPasswordButton.isEnabled = true
                }
            }
        }
        return binding.root
    }

}
