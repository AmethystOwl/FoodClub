package com.example.foodclub.restaurant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.foodclub.databinding.FragmentRestaurantInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RestaurantInfoFragment(private val address: String, private val workingHours: String) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRestaurantInfoBinding.inflate(inflater, container, false)
        binding.address = address
        binding.workingHours = workingHours

        return binding.root
    }


}
