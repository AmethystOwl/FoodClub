package com.example.foodclub.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentSecondBinding
import com.example.foodclub.onboarding.ViewPagerFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SecondFragment : Fragment() {
    @Inject
    lateinit var dataStore: DataStore<Preferences>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as MainActivity).hideBottomNavigation()
        val binding = FragmentSecondBinding.inflate(inflater, container, false)
        binding.nextButton.setOnClickListener {
            lifecycleScope.launch {
                dataStore.edit {
                    it[booleanPreferencesKey(getString(R.string.datastore_first_time_key))] = false
                }
            }
            findNavController().navigate(ViewPagerFragmentDirections.actionViewPagerFragmentToLoginFragment())
        }
        return binding.root
    }

}
