package com.example.foodclub.register.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodclub.databinding.FragmentFinishBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinishFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFinishBinding.inflate(inflater, container, false)
        binding.nextButton.setOnClickListener {
            findNavController().navigate(FinishFragmentDirections.actionFinishFragmentToHomeFragment())
        }
        return binding.root
    }

}
