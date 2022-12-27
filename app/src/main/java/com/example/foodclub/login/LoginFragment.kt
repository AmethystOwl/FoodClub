package com.example.foodclub.login

import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.LoginFragmentBinding
import com.example.foodclub.model.UserProfile
import com.example.foodclub.shared.UserViewModel
import com.example.foodclub.utils.Constants
import com.example.foodclub.utils.DataState
import com.example.foodclub.utils.LoadingDialog
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private val loginResultHandler = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data!!
            val credentials = Identity.getSignInClient(requireActivity()).getSignInCredentialFromIntent(intent)
            // credentials.id : Email Address
            // credentials.displayName : Full Name
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()

        lifecycleScope.launch {
            dataStore.data.collect {
                val auth = it[booleanPreferencesKey(getString(R.string.datastore_auth_key))]
                if (auth != null && auth == true) {
                    findNavController().
                    navigate(LoginFragmentDirections
                        .actionLoginFragmentToHomeFragment())
                }
                cancel()
            }
        }

        binding.googleSignInButton.setOnClickListener {
            val oneTapClient = Identity.getSignInClient(requireActivity())
            val signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest
                        .GoogleIdTokenRequestOptions
                        .builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .build()
            oneTapClient.beginSignIn(signUpRequest).addOnSuccessListener {
                loginResultHandler.launch(IntentSenderRequest.Builder(it.pendingIntent).build())
            }


        }

        binding.forgotPasswordGradiantTextView.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment())
        }
        binding.registerGradiantTextView.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        binding.loginButton.setOnClickListener {
            it.isEnabled = false
            viewModel.onDoneSettingError()
            if (isValidEmail() && isValidPassword()) {
                val email = binding.emailInputEditText.text?.toString()
                val password = binding.passwordInputEditText.text?.toString()

                viewModel.login(email!!, password!!)
            } else {
                it.isEnabled = true
            }

        }
        val loadingDialog = LoadingDialog(requireActivity())
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            when (errorMessage) {
                null -> binding.errorMessageTextView.visibility = View.GONE
                else -> {
                    binding.errorMessageTextView.visibility = View.VISIBLE
                    binding.errorMessageTextView.text = errorMessage
                }
            }
        }
        viewModel.loginState.observe(viewLifecycleOwner) { loginState ->
            when (loginState) {
                is DataState.Loading -> {
                    loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    loadingDialog.dismissDialog()
                    binding.loginButton.isEnabled = true
                    if (binding.keepMeLoggedInMaterialCheckBox.isChecked) {
                        lifecycleScope.launch {
                            dataStore.edit {
                                it[booleanPreferencesKey(getString(R.string.datastore_auth_key))] = true
                            }
                        }
                    }
                    val userProfile = loginState.data as UserProfile
                    if (userProfile.isUser) {
                        userViewModel.setCurrentUserProfile(userProfile)
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
                    }
                }
                is DataState.Error -> {
                    binding.loginButton.isEnabled = true
                    loadingDialog.dismissDialog()
                    Snackbar.make(
                        binding.coordinator,
                        "${loginState.exception.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is DataState.Invalid -> {
                    binding.loginButton.isEnabled = true
                    loadingDialog.dismissDialog()
                    if (loginState.data.toString().toInt() == Constants.LOGIN_INVALID_CREDENTIALS) {
                        viewModel.setError(getString(R.string.account_invalid_password))
                    } else if (loginState.data.toString().toInt() == Constants.LOGIN_NO_USER) {
                        viewModel.setError(getString(R.string.account_not_found))
                    }
                }
                else -> {
                    binding.loginButton.isEnabled = true
                    loadingDialog.dismissDialog()
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}
