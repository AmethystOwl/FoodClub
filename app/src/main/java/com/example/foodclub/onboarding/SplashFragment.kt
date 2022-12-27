package com.example.foodclub.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {
    @Inject
    lateinit var dataStore: DataStore<Preferences>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as MainActivity).hideBottomNavigation()
        lifecycleScope.launch {
            dataStore.data.collect {
                when (it[booleanPreferencesKey(getString(R.string.datastore_first_time_key))]) {
                    false -> {
                        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
                        cancel()
                    }
                    else -> {
                        findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToViewPagerFragment())
                        cancel()

                    }
                }
            }
        }

        return inflater.inflate(R.layout.fragment_splash, container, false)
    }


}
