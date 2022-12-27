package com.example.foodclub.restaurant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ahmadhamwi.tabsync.TabbedListMediator
import com.example.foodclub.adapter.MenuAdapter
import com.example.foodclub.adapter.MenuCategoryAdapter
import com.example.foodclub.cart.CartViewModel
import com.example.foodclub.databinding.FragmentRestaurantMenuBinding
import com.example.foodclub.model.CartItemUi
import com.example.foodclub.model.MenuCategoryUi
import com.example.foodclub.model.MenuItem
import com.example.foodclub.model.MenuItemUi
import com.example.foodclub.utils.DataState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RestaurantMenuFragment(
    private val restaurantId: String,
    private val restaurantName: String,
    private val restaurantLogo: String
) : Fragment() {
    private val viewModel: RestaurantMenuViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRestaurantMenuBinding.inflate(inflater, container, false)
        val onMenuItemClickListener = MenuAdapter.OnMenuItemClickListener {
            findNavController().navigate(
                RestaurantFragmentDirections.actionRestaurantFragmentToMealFragment(
                    restaurantId, restaurantName, restaurantLogo,it.id!!, it.present!!,it.qty?.toLong()!!)
            )
        }
        var cartItems = ArrayList<CartItemUi>()
        cartViewModel.getCart()

        cartViewModel.cartState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    val data = it.data.items!!
                    if (!data.isEmpty()) {
                        cartItems = data
                    }
                    viewModel.getMenu(restaurantId)
                }

                else -> {
                    viewModel.getMenu(restaurantId)
                }
            }
        }
        viewModel.menuState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {

                }
                is DataState.Success -> {
                    val data = it.data
                    val cats = ArrayList<String>()
                    data.forEach { test ->
                        if (!cats.contains(test.category)) {
                            cats.add(test.category!!)

                        }
                    }
                    val listSorted = HashMap<String, List<MenuItem>>()

                    binding.categoriesTabLayout.removeAllTabs()

                    cats.forEach { category ->
                        binding.categoriesTabLayout.addTab(
                            binding.categoriesTabLayout.newTab().setText(category)
                        )
                        listSorted[category] = data.filter { it.category == category }
                    }

                    val adapterList = ArrayList<MenuCategoryUi>()
                    listSorted.entries.forEach {
                        val menuItemUis: List<MenuItemUi> = it.value.map {
                            MenuItemUi(
                                it.id,
                                it.name,
                                it.description,
                                it.price,
                                it.likes,
                                it.imageUrl
                            )
                        }
                        adapterList.add(MenuCategoryUi(it.key, menuItemUis))
                    }
                    val adapter = MenuCategoryAdapter(onMenuItemClickListener, cartItems)
                    adapter.submitList(adapterList)

                    binding.apply {
                        restaurantMenuRecyclerView.adapter = adapter
                        TabbedListMediator(
                            restaurantMenuRecyclerView,
                            categoriesTabLayout,
                            cats.indices.toList(),
                            true
                        ).attach()

                    }

                }
                is DataState.Error -> {
                    Toast.makeText(
                        requireContext(),
                        it.exception.localizedMessage!!,
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                }
            }
        }
        return binding.root
    }


}