package com.example.foodclub.favorite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.adapter.FavoritesAdapter
import com.example.foodclub.databinding.FragmentFavoritesBinding
import com.example.foodclub.utils.DataState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FavoritesFragment : Fragment() {
    private var restaurantPosition = -1
    private val TAG = "FavoritesFragment"
    private var _binding: FragmentFavoritesBinding? = null
    private var adapter: FavoritesAdapter? = null
    private val viewModel: FavoritesViewModel by viewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getFavorites()
        viewModel.removeRestaurantState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    Log.d(TAG, "onViewCreated: $restaurantPosition")
                    adapter?.removeItem(restaurantPosition)
                }
                is DataState.Error -> {
                    Log.d(TAG, "onViewCreated: ${it.exception.message}")
                }

                else -> {}
            }

        }
        val onRemoveRestaurantClickListener = FavoritesAdapter.OnRemoveItemClickListener(
            onClickListener = object : (String, Int) -> Unit {
                override fun invoke(restaurantId: String, position: Int) {
                    restaurantPosition = position
                    viewModel.removeRestaurant(restaurantId)
                }

            }
        )
        viewModel.favoritesState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    adapter = FavoritesAdapter(
                        it.data, FavoritesAdapter.OnFavoritesItemClickListener { restaurantId ->
                            findNavController()
                                .navigate(
                                    FavoritesFragmentDirections
                                        .actionFavoritesFragmentToRestaurantFragment(restaurantId)
                                )
                        }, onRemoveRestaurantClickListener
                    )
                    binding.favoritesRecyclerView.adapter = adapter
                }
                is DataState.Error -> {
                    Log.d(TAG, "onViewCreated: ${it.exception.message}")
                }
                else -> {}
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
