package com.example.foodclub.register.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.databinding.FragmentLocationBinding
import com.example.foodclub.shared.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationFragment : Fragment() {
    val userViewModel: UserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLocationBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.userViewModel = userViewModel
        binding.setLocationButton.setOnClickListener {
            findNavController().navigate(LocationFragmentDirections.actionLocationFragmentToMapsFragment())
        }
        binding.nextButton.setOnClickListener {
            findNavController().navigate(LocationFragmentDirections.actionLocationFragmentToFinishFragment())
        }

        return binding.root
    }

}
