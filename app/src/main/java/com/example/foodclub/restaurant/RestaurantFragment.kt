package com.example.foodclub.restaurant

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentRestaurantBinding
import com.example.foodclub.model.Restaurant
import com.example.foodclub.utils.Constants
import com.example.foodclub.utils.DataState
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RestaurantFragment : Fragment() {

    private var _binding: FragmentRestaurantBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RestaurantViewModel by viewModels()
    private var restaurant: Restaurant? = null
    private var toRev = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRestaurantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val args = RestaurantFragmentArgs.fromBundle(requireArguments())
        val restaurantId = args.restaurantId


        binding.favoriteImageView.setOnClickListener {
            binding.favoriteImageView.isClickable = false
            viewModel.addToFavorites(restaurantId)
        }

        viewModel.currentRestaurant.observe(viewLifecycleOwner) {
            if (restaurant == null) {
                if (it != null) {
                    restaurant = it
                    initViews(restaurantId)
                } else {
                    viewModel.getRestaurantById(restaurantId)
                }

            } else {
                initViews(restaurantId)
            }
        }

        viewModel.restaurantState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {

                }
                is DataState.Success -> {
                    viewModel.setCurrentRestaurant(it.data)
                }
                is DataState.Error -> {
                    Log.w(tag, "onViewCreated: ", it.exception)
                }
                else -> {

                }
            }
        }

        viewModel.favorite.observe(viewLifecycleOwner) {
            if (it == null) {
                viewModel.getRestaurantFavoriteState(restaurantId)
            } else {
                if (it) {
                    binding.favoriteImageView.setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_favorite_red_24
                        )
                    )
                } else {
                    binding.favoriteImageView.setImageDrawable(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_favorite_border_24
                        )
                    )
                }
            }
        }
        viewModel.addToFavoritesState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    binding.favoriteImageView.isClickable = false
                }
                is DataState.Success -> {
                    binding.favoriteImageView.isClickable = true
                    val state = it.data
                    if (state == Constants.FAVORITE_STATE_ADDED) {
                        Toast.makeText(
                            requireContext(),
                            "${restaurant?.name} has been added to favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.setFavoriteState(true)
                    } else if (state == Constants.FAVORITE_STATE_REMOVED) {
                        Toast.makeText(
                            requireContext(),
                            "${restaurant?.name} has been removed from favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.setFavoriteState(false)

                    }
                    viewModel.onDoneObservingAddToFavoriteState()
                }
                is DataState.Error -> {
                    Log.d(tag, "addToFavoriteState: ${it.exception.message!!}")
                    binding.favoriteImageView.isClickable = true

                }
                else -> {
                    binding.favoriteImageView.isClickable = true
                }
            }
        }

        viewModel.favoriteState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    binding.favoriteImageView.isClickable = false
                }
                is DataState.Success -> {
                    viewModel.setFavoriteState(it.data!!)
                }
                is DataState.Error -> {
                    Log.d(tag, "favoriteState: ${it.exception.message!!}")
                    binding.favoriteImageView.isClickable = true
                }
                else -> {
                    binding.favoriteImageView.isClickable = true
                }
            }
        }
    }

    private fun initViews(restaurantId: String) {
        if (binding.item == null) {
            binding.item = restaurant
        }
        if (binding.viewPager.adapter == null) {
            val viewPager = binding.viewPager
            val adapter =
                RestaurantFragmentViewPagerAdapter(requireActivity(), restaurantId, restaurant!!)
            viewPager.adapter = adapter
            TabLayoutMediator(
                binding.restaurantTabLayout,
                binding.viewPager,
                true,
                true
            ) { tab, position ->
                run {
                    tab.text = when (position) {
                        0 -> getString(R.string.menu)
                        1 -> getString(R.string.info)
                        2 -> getString(R.string.reviews)
                        else -> getString(R.string.menu)
                    }
                }
            }.attach()
            if(!toRev && RestaurantFragmentArgs.fromBundle(requireArguments()).isToReview){
                binding.restaurantTabLayout.selectTab(binding.restaurantTabLayout.getTabAt(2))
                toRev = true
            }
        }

    }
}

/*     viewModel.getRestaurantById(restaurantId)
     viewModel.getRestaurantFavoriteState(restaurantId)
     val onMenuItemClickListener = MenuItemAdapter.OnMenuItemClickListener {
         findNavController().navigate(RestaurantFragmentDirections.actionRestaurantFragmentToMealFragment(restaurant, it))
     }
     viewModel.restaurantState.observe(viewLifecycleOwner) {
         when (it) {
             is DataState.Loading -> {

             }
             is DataState.Success -> {
                 restaurant = it.data
                 viewModel.getMenuCategories(restaurantId)
                 binding.item = restaurant

             }
             is DataState.Error -> {

             }
             else -> {

             }
         }
         viewModel.categoryState.observe(viewLifecycleOwner) {
             when (it) {
                 is DataState.Loading -> {

                 }
                 is DataState.Success -> {
                     binding.categoriesTabLayout.removeAllTabs()
                     it.data.forEach {
                         binding.categoriesTabLayout.addTab(binding.categoriesTabLayout.newTab().setText(it.name))
                     }

                     categoryAdapter = MenuCategoryAdapter(ArrayList(it.data), onMenuItemClickListener)
                     binding.restaurantMenuRecyclerView.adapter = categoryAdapter
                     TabbedListMediator(
                         binding.restaurantMenuRecyclerView,
                         binding.categoriesTabLayout,
                         it.data.indices.toList(),
                         true
                     ).attach()
                 }
                 is DataState.Error -> {

                 }
                 else -> {

                 }
             }
         }
         binding.favoriteImageView.setOnClickListener {
             viewModel.addToFavorites(restaurant.name!!)
         }
         viewModel.favoriteState.observe(viewLifecycleOwner) {
             when (it) {
                 is DataState.Loading -> {
                     binding.favoriteCardView.isClickable = false
                 }
                 is DataState.Success -> {
                     binding.favoriteCardView.isClickable = true
                     val isRestaurantFavored = it.data
                     if (isRestaurantFavored) {
                         binding.favoriteImageView.setImageDrawable(
                             AppCompatResources.getDrawable(
                                 requireContext(),
                                 R.drawable.ic_baseline_favorite_red_24
                             )
                         )
                     } else {
                         binding.favoriteCardView.isClickable = true
                         binding.favoriteImageView.setImageDrawable(
                             AppCompatResources.getDrawable(
                                 requireContext(),
                                 R.drawable.ic_baseline_favorite_white_24
                             )
                         )
                     }
                 }
                 is DataState.Error -> {
                     Log.d(TAG, "favorite: ${it.exception.message!!}")
                     binding.favoriteCardView.isClickable = true

                 }
                 else -> {
                     binding.favoriteCardView.isClickable = true
                 }
             }
         }
         viewModel.addToFavoritesState.observe(viewLifecycleOwner) {
             when (it) {
                 is DataState.Loading -> {
                     binding.favoriteCardView.isClickable = false
                 }
                 is DataState.Success -> {
                     viewModel.getRestaurantFavoriteState(restaurantId)
                     val state = it.data
                     if (state == Constants.FAVORITE_STATE_ADDED) {
                         Toast.makeText(requireContext(), "${restaurant.name} has been added to favorites", Toast.LENGTH_SHORT).show()

                     } else if (state == Constants.FAVORITE_STATE_REMOVED) {
                         Toast.makeText(requireContext(), "${restaurant.name} has been removed from favorites", Toast.LENGTH_SHORT).show()

                     }
                     binding.favoriteCardView.isClickable = true

                 }
                 is DataState.Error -> {
                     viewModel.getRestaurantFavoriteState(restaurantId)
                     Log.d(TAG, "favorite: ${it.exception.message!!}")
                     binding.favoriteCardView.isClickable = true

                 }
                 else -> {
                     binding.favoriteCardView.isClickable = true
                 }
             }
         }

     }
*/
