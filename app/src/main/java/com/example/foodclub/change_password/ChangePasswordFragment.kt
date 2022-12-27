package com.example.foodclub.change_password

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentChangePasswordBinding
import com.example.foodclub.shared.UserViewModel
import com.example.foodclub.utils.DataState
import com.example.foodclub.utils.hideSoftKeyboard
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        binding.backCardView.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.changePasswordButton.setOnClickListener {
            val newPass = binding.newPasswordTextInputEditText.text
            val confirmPass = binding.confirmPasswordTextInputEditText.text
            val curPass = binding.currentPasswordTextInputEditText.text
            if (curPass.isNullOrEmpty() || curPass.isBlank()) {
                binding.currentPasswordTextInputLayout.error = getString(R.string.cur_pass_enter)
                binding.currentPasswordTextInputLayout.requestFocus()
                return@setOnClickListener
            }
            if (newPass.isNullOrEmpty() || newPass.isBlank()) {
                binding.newPasswordTextInputLayout.error = getString(R.string.new_pass_enter)
                binding.newPasswordTextInputLayout.requestFocus()
                return@setOnClickListener
            }
            if (confirmPass.isNullOrEmpty() || confirmPass.isBlank()) {
                binding.confirmPasswordTextInputLayout.error = getString(R.string.new_pass_reenter)
                binding.confirmPasswordTextInputLayout.requestFocus()
                return@setOnClickListener
            }
            if (newPass.toString() != confirmPass.toString()) {
                binding.confirmPasswordTextInputLayout.error = getString(R.string.pass_no_match)
                binding.confirmPasswordTextInputLayout.requestFocus()
                return@setOnClickListener
            }
            binding.currentPasswordTextInputLayout.error = null
            binding.newPasswordTextInputLayout.error = null
            binding.confirmPasswordTextInputLayout.error = null
            hideSoftKeyboard()
            viewModel.changePassword(curPass.toString(), newPass.toString())
        }

        viewModel.passwordState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    binding.changePasswordButton.isEnabled = false
                }
                is DataState.Success -> {
                    binding.changePasswordButton.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.pass_changed),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.onDoneObservingPassword()
                }
                is DataState.Error -> {
                    viewModel.onDoneObservingPassword()
                    binding.changePasswordButton.isEnabled = true
                    when (it.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            when (it.exception.errorCode) {
                                "ERROR_USER_TOKEN_EXPIRED" -> {
                                    AlertDialog.Builder(requireContext())
                                        .setMessage(getString(R.string.session_expired))
                                        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                            dialog.dismiss()
                                        }.show()
                                }
                                else -> {
                                    Toast.makeText(
                                        requireContext(),
                                        it.exception.localizedMessage!!,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        is FirebaseAuthWeakPasswordException -> {
                            AlertDialog.Builder(requireContext())
                                .setMessage(it.exception.reason!!)
                                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                }.show()
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            AlertDialog.Builder(requireContext())
                                .setMessage(getString(R.string.old_pass_invalid))
                                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                }.show()
                        }
                        else -> {
                            AlertDialog.Builder(requireContext())
                                .setMessage(it.exception.localizedMessage)
                                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                    dialog.dismiss()
                                }.show()
                        }
                    }
                }
                else -> {
                    binding.changePasswordButton.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
