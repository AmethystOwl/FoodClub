package com.example.foodclub.profile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentProfileBinding
import com.example.foodclub.databinding.LayoutBottomLanguageBinding
import com.example.foodclub.shared.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()

    @Inject
    lateinit var dataStore: DataStore<Preferences>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        val main = requireActivity() as MainActivity
        main.setSupportActionBar(binding.toolBar)
        main.setCurrentItemActive(1)
        main.showBottomNavigation()

        when (userViewModel.currentUserProfile.value) {
            null -> {
                // show login dialog
            }
            else -> {
                binding.user = userViewModel.currentUserProfile.value!!
            }


        }
        binding.favoritesConstraint.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToFavoritesFragment())
        }
        binding.changePasswordConstraint.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToChangePasswordFragment())
        }
        binding.addressBookConstraint.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToAddressBookFragment())
        }
        binding.ordersConstraint.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToOrdersFragment())

        }
        binding.logoutGradiantTextView.setOnClickListener {
            lifecycleScope.launch {
                dataStore.edit {
                    it[booleanPreferencesKey(getString(R.string.datastore_auth_key))] = false
                }
            }
            userViewModel.logOut()
            main.restartActivity()

        }
        binding.languageCardView.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val languageLayoutBinding =
                LayoutBottomLanguageBinding.inflate(layoutInflater)
            bottomSheetDialog.setContentView(languageLayoutBinding.root)
            bottomSheetDialog.show()
            val current = requireActivity()
                .getSharedPreferences(
                    getString(R.string.app_lang_pref),
                    Activity.MODE_PRIVATE
                )
                .getString(getString(R.string.app_lang_pref_key), "")
            when (current) {
                getString(R.string.english_code) -> languageLayoutBinding.englishRadioButton.isChecked =
                    true
                "" -> languageLayoutBinding.englishRadioButton.isChecked = true
                getString(R.string.arabic_code) -> languageLayoutBinding.arabicRadioButton.isChecked =
                    true

            }


            languageLayoutBinding.englishRadioButton.setOnClickListener {
                if (current == getString(R.string.arabic_code)) {
                    main.changeLanguage(getString(R.string.english_code))
                }
                bottomSheetDialog.dismiss()
            }
            languageLayoutBinding.arabicRadioButton.setOnClickListener {
                if (current == getString(R.string.english_code) || current == "") {
                    main.changeLanguage(getString(R.string.arabic_code))
                }
                bottomSheetDialog.dismiss()
            }

        }
        return binding.root
    }


}
