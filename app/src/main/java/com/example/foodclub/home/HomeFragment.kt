package com.example.foodclub.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.adapter.CuisineAdapter
import com.example.foodclub.adapter.RestaurantAdapter
import com.example.foodclub.databinding.HomeFragmentBinding
import com.example.foodclub.databinding.LayoutBottomFilterBinding
import com.example.foodclub.model.Cuisine
import com.example.foodclub.model.Restaurant
import com.example.foodclub.shared.UserViewModel
import com.example.foodclub.utils.Constants
import com.example.foodclub.utils.DataState
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var restaurantAdapter: RestaurantAdapter? = null
    private var cuisineAdapter: CuisineAdapter? = null
    private val viewModel: HomeViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val TAG = "HomeFragment"
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        main.setCurrentItemActive(0)
        main.showBottomNavigation()
        val query = viewModel.getDefaultQuery(false)
        val options =
            FirestoreRecyclerOptions
                .Builder<Restaurant>()
                .setQuery(query, Restaurant::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()
        restaurantAdapter = RestaurantAdapter(options, RestaurantAdapter.OnRestaurantClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToRestaurantFragment(
                    it
                )
            )
        })


        binding.restaurantsRecyclerView.adapter = restaurantAdapter
        binding.restaurantsRecyclerView.itemAnimator = null
        binding.filterCardView.setOnClickListener {
            val bsd =
                BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val bottomSheetViewBinding =
                LayoutBottomFilterBinding.inflate(layoutInflater, null, false)
            bsd.setContentView(bottomSheetViewBinding.root)
            bsd.show()
            bottomSheetViewBinding.closeImageButton.setOnClickListener {
                bsd.dismiss()
            }
            bottomSheetViewBinding.filterButton.setOnClickListener {
                val checked = ArrayList<String>()
                if (bottomSheetViewBinding.freeDeliveryChip.isChecked) {
                    checked.add(Constants.FIELD_FREE_DELIVERY)
                }
                if (bottomSheetViewBinding.openNowChip.isChecked) {
                    checked.add(Constants.FIELD_OPEN_NOW)
                }
                if (bottomSheetViewBinding.ratingsChip.isChecked) {
                    checked.add(Constants.FIELD_NO_REVIEWS)
                }
                if (bottomSheetViewBinding.aToZChip.isChecked) {
                    checked.add(Constants.A_TO_Z)
                }
                val filteredQuery = viewModel.getFilteredQuery(checked)
                val filteredOptions = FirestoreRecyclerOptions
                    .Builder<Restaurant>()
                    .setQuery(filteredQuery, Restaurant::class.java)
                    .build()
                restaurantAdapter?.updateOptions(filteredOptions)

                bsd.dismiss()
            }
        }

        if (userViewModel.currentUserProfile.value == null) {
            userViewModel.fetchCurrentUserProfile()
        }
        userViewModel.currentUserProfileState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {

                }
                is DataState.Success -> {
                    when (it.data) {
                        null -> {}
                        else -> {
                            userViewModel.setCurrentUserProfile(it.data)
                        }
                    }
                }
                is DataState.Error -> {

                }
                else -> {

                }

            }
        }
        val cuisines = ArrayList<Cuisine>()
        cuisines.apply {
            add(
                Cuisine(
                    "All",
                    BitmapFactory.decodeResource(resources, R.drawable.all),
                    true
                )
            )
            add(
                Cuisine(
                    "Burger",
                    BitmapFactory.decodeResource(resources, R.drawable.burger),
                    false
                )
            )
            add(
                Cuisine(
                    "Pasta",
                    BitmapFactory.decodeResource(resources, R.drawable.pasta),
                    false
                )
            )
            add(
                Cuisine(
                    "Pizza",
                    BitmapFactory.decodeResource(resources, R.drawable.pizza),
                    false
                )
            )
            add(
                Cuisine(
                    "Beef",
                    BitmapFactory.decodeResource(resources, R.drawable.beef),
                    false
                )
            )
            add(
                Cuisine(
                    "Chicken",
                    BitmapFactory.decodeResource(resources, R.drawable.chicken),
                    false
                )
            )
        }
        val onCuisineClickListener =
            CuisineAdapter.OnCuisineClickListener(object : (Cuisine, Int) -> Unit {
                override fun invoke(cuisine: Cuisine, position: Int) {
                    if (cuisineAdapter?.selectItem(position) == true) {
                        val filteredQuery: Query
                        if (cuisine.name.lowercase() == "all") {
                            filteredQuery = viewModel.getDefaultQuery(false)
                        } else {
                            filteredQuery = viewModel.filterRestaurantsQuery(cuisine.name)
                        }
                        val filteredOptions = FirestoreRecyclerOptions
                            .Builder<Restaurant>()
                            .setQuery(filteredQuery, Restaurant::class.java)
                            .build()
                        restaurantAdapter?.updateOptions(filteredOptions)
                    }
                }

            })
        cuisineAdapter = CuisineAdapter(cuisines, onCuisineClickListener)
        binding.cuisineRecyclerView.adapter = cuisineAdapter



        binding.searchCardView.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProductFragment())
        }
    }

    override fun onStart() {
        super.onStart()
        binding.restaurantsShimmerFrameLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.restaurantsShimmerFrameLayout.stopShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
