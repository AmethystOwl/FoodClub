package com.example.foodclub.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.OrderItemUi
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private var _ordersState = MutableLiveData<DataState<ArrayList<OrderItemUi>>>()
    val ordersState : LiveData<DataState<ArrayList<OrderItemUi>>> get() = _ordersState

    fun getOrders() {
        viewModelScope.launch {
            repo.getOrders().collect{
                _ordersState.value = it
            }
        }
    }

}