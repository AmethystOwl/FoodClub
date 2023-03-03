package com.example.foodclub.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.adapter.OrdersAdapter
import com.example.foodclub.databinding.FragmentOrdersBinding
import com.example.foodclub.model.OrderItemUi
import com.example.foodclub.utils.DataState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding: FragmentOrdersBinding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels()
    private var adapter: OrdersAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val main = requireActivity() as MainActivity
        main.hideBottomNavigation()
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.getOrders()
        viewModel.ordersState.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Loading -> {
                    showNoOrders(true)
                }
                is DataState.Success -> {
                    val array = it.data
                    if (array.isEmpty()) {
                        showNoOrders(true)
                    } else {
                        showNoOrders(false)
                        adapter = OrdersAdapter(OrdersAdapter.OnClickListener(object :
                                (OrderItemUi) -> Unit {
                            override fun invoke(orderItemUi: OrderItemUi) {
                                findNavController().navigate(
                                    OrdersFragmentDirections.actionOrdersFragmentToViewOrderFragment(
                                        orderItemUi
                                    )
                                )
                            }

                        }))
                        adapter?.submitList(array)
                        binding.ordersRecyclerView.adapter = adapter
                    }
                }
                is DataState.Error -> {
                    Toast.makeText(requireContext(), "An error has occurred", Toast.LENGTH_LONG)
                        .show()
                }
                else -> {}
            }
        }

    }

    private fun showNoOrders(isEmpty: Boolean) {
        when (isEmpty) {
            true -> {
                binding.emptyOrdersLayout.visibility = View.VISIBLE
                binding.ordersRecyclerView.visibility = View.GONE
            }
            false -> {
                binding.emptyOrdersLayout.visibility = View.GONE
                binding.ordersRecyclerView.visibility = View.VISIBLE
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
