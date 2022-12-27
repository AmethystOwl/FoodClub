package com.example.foodclub.meal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.CartItem
import com.example.foodclub.model.MenuItemUi
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MealViewModel @Inject constructor(private val repo: Repository) : ViewModel() {

    private var _menuItem = MutableLiveData<MenuItemUi>()
    val menuItem: LiveData<MenuItemUi> get() = _menuItem

    private var _total = MutableLiveData<Double>()
    val total: LiveData<Double> get() = _total


    private var _menuItemUiState = MutableLiveData<DataState<MenuItemUi>>()
    val menuItemUiState: LiveData<DataState<MenuItemUi>> get() = _menuItemUiState


    private var _addToCartState = MutableLiveData<DataState<Int>?>()
    val addToCartState: LiveData<DataState<Int>?> get() = _addToCartState


    fun addToCart(cartItem: CartItem) {
        viewModelScope.launch {
            repo.addToCart(cartItem).collect {
                _addToCartState.value = it
            }
        }
    }

    fun getMenuItemById(restaurantId: String, menuItemId: String) {
        viewModelScope.launch {
            repo.getMenuItemById(restaurantId, menuItemId).collect {
                _menuItemUiState.value = it
            }
        }
    }

    fun addToFreshCart(cartItem: CartItem) {
        viewModelScope.launch {
            repo.addToFreshCart(cartItem).collect {
                _addToCartState.value = it
            }
        }
    }

    fun onDoneAddingToCart() {
        _addToCartState.value = null
    }

    fun setItem(menuItemUi: MenuItemUi) {
        _menuItem.value = menuItemUi
    }

    fun setTotal(quantity: Int) {
        _menuItem.value?.price?.let {
            _total.value = quantity.toDouble() * _menuItem.value?.price!!
        }
    }
}
