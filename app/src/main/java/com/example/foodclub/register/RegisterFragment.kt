package com.example.foodclub.register

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.RegisterFragmentBinding
import com.example.foodclub.model.UserProfile
import com.example.foodclub.utils.DataState
import com.example.foodclub.utils.LoadingDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: RegisterFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        val loadingDialog = LoadingDialog(requireActivity())
        binding.ccp.registerCarrierNumberEditText(binding.phoneInputEditText)
        binding.dateOfBirthInputLayout.setEndIconOnClickListener {
            showCalenderVIew()
        }
        binding.registerButton.setOnClickListener {
            if (isValidName() &&
                isValidEmail() &&
                isValidPassword() &&
                isValidConfirmPassword() &&
                isValidDateOfBirth() &&
                isValidPhone()
            ) {
                registerUser()
            }
        }

        viewModel.registerState.observe(viewLifecycleOwner) { userState ->
            when (userState) {
                is DataState.Loading -> {
                    loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    loadingDialog.dismissDialog()
                    findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToPhotoFragment())
                    Log.d("userState", "onViewCreated: ${userState.data.name}")
                }
                is DataState.Error -> {
                    loadingDialog.dismissDialog()
                    Snackbar.make(
                        requireContext(),
                        binding.coordinator,
                        "${userState.exception.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Log.d("userState", "onViewCreated: ${userState.exception.message}")
                }
                else -> {
                    loadingDialog.dismissDialog()
                }
            }

        }
    }

    private fun registerUser() {
        val name = binding.nameInputEditText.text?.toString()
        val email = binding.emailInputEditText.text?.toString()
        val password = binding.passwordInputEditText.text?.toString()
        val phone = binding.phoneInputEditText.text?.toString()
        val fullPhone =
            binding.ccp.defaultCountryCodeWithPlus + phone?.filter { !it.isWhitespace() }!!

        val dateOfBirth = binding.dateOfBirthInputEditText.text?.toString()
        if (!name.isNullOrBlank()
            && !email.isNullOrBlank()
            && !password.isNullOrBlank()
            && phone.isNotBlank()
            && !dateOfBirth.isNullOrBlank()
        ) {
            val user = UserProfile(
                uId = null,
                name = name,
                email = email,
                dateOfBirth = dateOfBirth,
                phoneNumber = fullPhone,
                location = null,
            )
            viewModel.registerUser(user, password)
        }
    }

    private fun showCalenderVIew() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.dateOfBirthInputEditText.setText(
                    StringBuilder().append(day).append("/")
                        .append(month + 1).append("/").append(year)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun isValidName(): Boolean {
        return when {
            binding.nameInputEditText.text.isNullOrBlank() -> {
                binding.nameInputLayout.isErrorEnabled = true
                binding.nameInputLayout.error =
                    getString(R.string.name_empty)
                false
            }
            binding.nameInputEditText.text?.length!! < 3 -> {
                binding.nameInputLayout.isErrorEnabled = true
                binding.nameInputLayout.error =
                    getString(R.string.name_too_short)
                false
            }
            else -> {
                binding.nameInputLayout.error = null
                binding.nameInputLayout.isErrorEnabled = false
                true
            }
        }
    }

    private fun isValidEmail(): Boolean {
        return when {
            binding.emailInputEditText.text.isNullOrBlank() -> {
                binding.emailInputLayout.error = getString(R.string.email_null)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(binding.emailInputEditText.text?.toString()!!)
                .matches() -> {
                binding.emailInputLayout.error = getString(R.string.invalid_email_format)
                false
            }
            else -> {
                binding.emailInputLayout.error = null
                binding.emailInputLayout.isErrorEnabled = false
                true
            }
        }
    }

    private fun isValidPassword(): Boolean {
        return when {
            binding.passwordInputEditText.text?.length!! < 6 -> {
                binding.passwordInputLayout.error = getString(R.string.password_too_short)
                false
            }
            else -> {
                binding.passwordInputLayout.error = null
                binding.passwordInputLayout.isErrorEnabled = false
                true
            }
        }
    }

    private fun isValidConfirmPassword(): Boolean {
        return when {
            binding.confirmPasswordInputEditText.text.toString() != binding.passwordInputEditText.text.toString() -> {
                binding.confirmPasswordInputLayout.error =
                    getString(R.string.passwords_dont_match)
                false
            }
            else -> {
                binding.confirmPasswordInputLayout.error = null
                binding.confirmPasswordInputLayout.isErrorEnabled = false
                true
            }
        }
    }

    private fun isValidDateOfBirth(): Boolean {
        return when {
            binding.dateOfBirthInputEditText.text.isNullOrBlank() -> {
                binding.dateOfBirthInputLayout.errorIconDrawable = null
                binding.dateOfBirthInputLayout.error =
                    getString(R.string.date_of_birth_blank)
                binding.dateOfBirthInputLayout.endIconDrawable = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_outline_calendar_month_24
                )
                false
            }
            else -> {
                binding.dateOfBirthInputLayout.error = null
                binding.dateOfBirthInputLayout.isErrorEnabled = false
                true
            }

        }
    }

    private fun isValidPhone(): Boolean {
        return when {
            binding.phoneInputEditText.text.isNullOrBlank() -> {
                binding.phoneInputLayout.error =
                    getString(R.string.phone_blank)
                false
            }
            !binding.ccp.isValidFullNumber -> {
                binding.phoneInputLayout.error =
                    getString(R.string.phone_invalid)
                false
            }
            else -> {
                binding.phoneInputLayout.error = null
                binding.phoneInputLayout.isErrorEnabled = false
                true
            }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
