package com.example.foodclub.restaurant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.MenuItem
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantMenuViewModel @Inject constructor(private val repo: Repository) : ViewModel() {

    private var _menuState = MutableLiveData<DataState<List<MenuItem>>>()
    val menuState: LiveData<DataState<List<MenuItem>>> get() = _menuState
    fun getMenu(restaurantId: String) {
        viewModelScope.launch {
            repo.getMenu(restaurantId).collect {
                _menuState.value = it
            }
        }
    }

}
