package com.example.foodclub.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.CartItemUi
import com.example.foodclub.model.CartUi
import com.example.foodclub.model.OrderItem
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    var cartRestaurantId = MutableLiveData<String>()

    var cartRestaurantLogo = MutableLiveData<String>()

    var cartRestaurantName = MutableLiveData<String>()

    var cartItemCount = MutableLiveData<Int>()

    var cartTotalPrice = MutableLiveData<Double>()


    private var _placeOrderState = MutableLiveData<DataState<Int>>()
    val placeOrderState: LiveData<DataState<Int>> get() = _placeOrderState

    private var _cartState = MutableLiveData<DataState<CartUi>>()
    val cartState: LiveData<DataState<CartUi>> get() = _cartState

    private var _deletionState = MutableLiveData<DataState<Int>?>()
    val deletionState: LiveData<DataState<Int>?> get() = _deletionState
    fun getCart() {
        viewModelScope.launch {
            repo.getCartQuery().collect {
                _cartState.value = it
            }
        }
    }

    fun removeItem(cartItemUi: CartItemUi) {
        viewModelScope.launch {
            repo.removeCartItem(cartItemUi).collect {
                _deletionState.value = it
            }
        }
    }

    fun removeCart() {
        viewModelScope.launch {
            repo.removeCart().collect {
                _deletionState.value = it
            }
        }

    }

    fun onDoneRemoving() {
        _deletionState.value = null
    }

    fun removeItems(cartList: ArrayList<CartItemUi>) {
        viewModelScope.launch {
            cartList.forEach {
                repo.removeCartItem(it).collect {
                    _deletionState.value = it

                }
            }
        }.invokeOnCompletion {
            viewModelScope.launch {
                repo.removeCart().collect {
                    _deletionState.value = it
                }
            }

        }
    }

    fun placeOrder(orderItem: OrderItem) {
        viewModelScope.launch {
            repo.placeOrder(orderItem).collect {
                _placeOrderState.value = it
            }
        }
    }
}
