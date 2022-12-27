package com.example.foodclub.order.view_order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.foodclub.databinding.FragmentViewOrderBinding

class ViewOrderFragment : Fragment() {
    private var _binding: FragmentViewOrderBinding? = null
    private val binding: FragmentViewOrderBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        val orderMenuItemArg = ViewOrderFragmentArgs.fromBundle(requireArguments()).orderItemUi
        binding.orderItemUi = orderMenuItemArg
        binding.buyAgainCardView.setOnClickListener {
            findNavController().navigate(
                ViewOrderFragmentDirections.actionViewOrderFragmentToMealFragment(
                    orderMenuItemArg.restaurant?.id!!, orderMenuItemArg.restaurant.name!!,
                    orderMenuItemArg.restaurant.iconImageUrl!!, orderMenuItemArg.mealId!!, false
                )
            )
        }
        binding.writeReviewCardView.setOnClickListener {
            findNavController().navigate(
                ViewOrderFragmentDirections.actionViewOrderFragmentToRestaurantFragment(
                    orderMenuItemArg.restaurant?.id!!, true
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}