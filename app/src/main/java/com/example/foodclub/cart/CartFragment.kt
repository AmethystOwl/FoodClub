package com.example.foodclub.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.adapter.CartAdapter
import com.example.foodclub.databinding.FragmentCartBinding
import com.example.foodclub.model.CartItemUi
import com.example.foodclub.model.OrderItem
import com.example.foodclub.model.OrderItemsDetails
import com.example.foodclub.utils.Constants
import com.example.foodclub.utils.DataState
import com.example.foodclub.utils.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private var adapter: CartAdapter? = null
    private val viewModel: CartViewModel by viewModels()
    private var currentPos: Int = -1
    private lateinit var cartList: ArrayList<CartItemUi>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val main = requireActivity() as MainActivity
        main.setCurrentItemActive(2)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.cartViewModel = viewModel
        cartList = ArrayList()
        viewModel.getCart()
        val loadingDialog = LoadingDialog(requireActivity())
        binding.materialToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.removeAllMaterialButton.setOnClickListener {
            viewModel.removeItems(cartList)
        }
        binding.checkoutButton.root.setOnClickListener {
            val items: ArrayList<OrderItemsDetails> = cartList.map {
                OrderItemsDetails(
                    mealId = it.mealId,
                    price = it.price,
                    quantity = it.quantity,
                    total = it.total
                )
            } as ArrayList<OrderItemsDetails>

            val orderItem = OrderItem(
                orderId = null,
                userId = null,
                restaurantId = viewModel.cartRestaurantId.value!!,
                items = items
            )
            viewModel.placeOrder(orderItem)
        }

        viewModel.cartState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    // loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    //   loadingDialog.dismissDialog()
                    val data = it.data
                    if (data.items?.size == 0) {
                        showNoCart(true)
                    } else {
                        showNoCart(false)
                        var subtotal = 0.0
                        var qty = 0L
                        it.data.items?.forEach {
                            subtotal += it.total!!
                            qty += it.quantity!!
                        }
                        viewModel.cartItemCount.value = qty.toInt()
                        viewModel.cartTotalPrice.value = subtotal
                        viewModel.cartRestaurantName.value = data.restaurant?.name!!
                        viewModel.cartRestaurantLogo.value = data.restaurant.iconImageUrl!!
                        viewModel.cartRestaurantId.value = data.restaurant.id!!

                        adapter = CartAdapter(
                            CartAdapter.OnCartItemRemoveClickListener(object :
                                    (CartItemUi, Int) -> Unit {
                                override fun invoke(cartItemUi: CartItemUi, position: Int) {
                                    currentPos = position
                                    viewModel.removeItem(cartItemUi)
                                    /*data.items?.remove(cartItemUi)*/
                                }

                            }),
                            CartAdapter.OnCartItemEditClickListener(object :
                                    (CartItemUi, Int) -> Unit {
                                override fun invoke(cartItemUi: CartItemUi, position: Int) {
                                    findNavController().navigate(
                                        CartFragmentDirections.actionCartFragmentToMealFragment(
                                            data.restaurant.id!!,
                                            data.restaurant.name!!,
                                            data.restaurant.iconImageUrl!!,
                                            cartItemUi.mealId!!,
                                            true,
                                            cartItemUi.quantity!!
                                        )
                                    )

                                }

                            })
                        )
                        cartList = data.items!!
                        adapter?.submitList(cartList)
                        binding.cartRecyclerView.adapter = adapter
                    }

                }
                is DataState.Error -> {
                    //loadingDialog.dismissDialog()
                    Toast.makeText(
                        requireContext(),
                        "An error's occurred: ${it.exception.localizedMessage!!}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                else -> {
                    //  loadingDialog.dismissDialog()
                }
            }
        }

        viewModel.deletionState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    // loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    //  loadingDialog.dismissDialog()
                    /*    cartList.removeAt(currentPos)
                        adapter?.notifyItemRemoved(currentPos)
                        adapter?.submitList(cartList)*/
                    if (it.data == Constants.EMPTY_CART) {
                        viewModel.removeCart()
                        showNoCart(true)
                    }
                    viewModel.onDoneRemoving()
                    viewModel.getCart()
                    loadingDialog.dismissDialog()

                }
                is DataState.Error -> {
                    //loadingDialog.dismissDialog()
                    Toast.makeText(
                        requireContext(),
                        it.exception.localizedMessage!!,
                        Toast.LENGTH_LONG
                    ).show()

                }
                else -> {
                    // loadingDialog.dismissDialog()

                }
            }
        }
        viewModel.placeOrderState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    loadingDialog.startDialog()
                }
                is DataState.Success -> {
                    Toast.makeText(
                        requireContext(),
                        "Order's been placed successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    viewModel.removeItems(cartList)
                }
                is DataState.Error -> {
                    loadingDialog.dismissDialog()
                    Toast.makeText(
                        requireContext(),
                        "An error's occurred: ${it.exception.message!!}",
                        Toast.LENGTH_LONG
                    )
                        .show()

                }
                else -> {}
            }
        }
    }

    private fun showNoCart(isEmpty: Boolean) {
        when (isEmpty) {
            true -> {
                binding.emptyCartLayout.visibility = View.VISIBLE
                binding.nestedScrollView.visibility = View.GONE
                binding.checkoutButton.root.visibility = View.GONE
            }
            false -> {
                binding.emptyCartLayout.visibility = View.GONE
                binding.nestedScrollView.visibility = View.VISIBLE
                binding.checkoutButton.root.visibility = View.VISIBLE
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
