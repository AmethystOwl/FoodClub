package com.example.foodclub.meal

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.R
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.cart.CartViewModel
import com.example.foodclub.databinding.FragmentMealBinding
import com.example.foodclub.model.CartItem
import com.example.foodclub.utils.Constants
import com.example.foodclub.utils.DataState
import com.example.foodclub.utils.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MealFragment : Fragment() {
    private var _binding: FragmentMealBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MealViewModel by activityViewModels()
    private val cartViewModel: CartViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        val qty = MealFragmentArgs.fromBundle(requireArguments()).qty
        val present = MealFragmentArgs.fromBundle(requireArguments()).present
        val restaurantId = MealFragmentArgs.fromBundle(requireArguments()).restaurantId
        val menuItemId = MealFragmentArgs.fromBundle(requireArguments()).menuItemId

        binding.mealViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.materialToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.plusImageView.setOnClickListener {
            binding.quantityTextView.text =
                binding.quantityTextView.text.toString().toInt().plus(1).toString()

        }
        binding.minusImageView.visibility = View.INVISIBLE
        binding.minusImageView.setOnClickListener {
            binding.quantityTextView.text =
                binding.quantityTextView.text.toString().toInt().minus(1).toString()
        }
        binding.quantityTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val count = p0?.toString()?.toInt()!!
                viewModel.setTotal(count)
                when {
                    count > 9 -> {
                        binding.plusImageView.visibility = View.INVISIBLE
                    }
                    count < 2 -> {
                        binding.minusImageView.visibility = View.INVISIBLE
                    }
                    else -> {
                        binding.plusImageView.visibility = View.VISIBLE
                        binding.minusImageView.visibility = View.VISIBLE

                    }

                }
            }

        })
        binding.addToCartButton.setOnClickListener {
            viewModel.addToCart(
                CartItem(
                    restaurantId, menuItemId, binding.quantityTextView.text.toString().toLong()
                )
            )
        }

        binding.toAR.setOnClickListener {
            findNavController().navigate(MealFragmentDirections.toPizzaAR())
        }
        viewModel.getMenuItemById(restaurantId, menuItemId)
        val loadingDialog = LoadingDialog(requireActivity())

        viewModel.menuItemUiState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    loadingDialog.dismissDialog()
                    val data = it.data
                    data.qty = qty.toInt()
                    data.present = present
                    viewModel.setItem(data)
                    viewModel.setTotal(binding.quantityTextView.text.toString().toInt())
                }
                is DataState.Error -> {
                    loadingDialog.dismissDialog()

                }
                else -> {
                    loadingDialog.dismissDialog()

                }
            }
        }
        viewModel.addToCartState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    loadingDialog.dismissDialog()
                    Toast.makeText(
                        requireContext(), getString(R.string.item_added_to_cart), Toast.LENGTH_SHORT
                    ).show()
                    viewModel.onDoneAddingToCart()
                    cartViewModel.getCart()
                }
                is DataState.Error -> {
                    loadingDialog.dismissDialog()

                }
                is DataState.Invalid -> {
                    loadingDialog.dismissDialog()

                    if (it.data == Constants.RESTAURANT_MISMATCH) {
                        val alertDialog = AlertDialog.Builder(requireContext())
                            .setView(R.layout.delete_cart_layout)
                            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                                dialogInterface.dismiss()

                            }
                            .setPositiveButton("Delete Cart") { dialogInterface: DialogInterface, _: Int ->
                                // delete THEN add new
                                viewModel.addToFreshCart(
                                    CartItem(
                                        restaurantId,
                                        menuItemId,
                                        binding.quantityTextView.text.toString().toLong()
                                    )
                                )
                                dialogInterface.dismiss()
                            }.create()
                        alertDialog.show()
                    }
                }
                else -> {
                    loadingDialog.dismissDialog()

                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


/*   private fun addItem(menuItemPrefsCat: MenuItemPreferencesCategory) {
       val item: ExpandingItem = binding.expandingListMain.createNewItem(R.layout.food_expanding_layout)
       (item.findViewById<TextView>(R.id.categoryTextView)).text = menuItemPrefsCat.title
       item.createSubItems(menuItemPrefsCat.items.size)
       for (i in 0 until item.subItemsCount) {
           val view = item.getSubItemView(i)
           configureSubItem(item, view, menuItemPrefsCat.items[i])
       }
   }

   private fun configureSubItem(item: ExpandingItem, view: View, subTitle: MenuItemPreferences) {
       (view.findViewById<TextView>(R.id.itemTitleMaterialCheckBox)).text = subTitle.name
       (view.findViewById<TextView>(R.id.itemPriceMaterialTextView)).text = getString(R.string.meal_price, subTitle.price.toString())

   }
*/

}
