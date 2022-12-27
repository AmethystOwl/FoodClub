package com.example.foodclub.addressbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentAddressBookBinding
import com.example.foodclub.shared.UserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AddressBookFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAddressBookBinding.inflate(inflater, container, false)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        binding.lifecycleOwner=viewLifecycleOwner
        binding.viewModel = userViewModel
        binding.backCardView.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editMaterialButton.setOnClickListener {
            findNavController().navigate(AddressBookFragmentDirections.actionAddressBookFragmentToMapsFragment())
        }

        return binding.root
    }


}
