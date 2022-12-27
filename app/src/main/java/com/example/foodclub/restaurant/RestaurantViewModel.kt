package com.example.foodclub.restaurant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.Restaurant
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private var _addToFavoritesState = MutableLiveData<DataState<Int>?>()
    val addToFavoritesState: LiveData<DataState<Int>?> get() = _addToFavoritesState

    private var _favoriteState = MutableLiveData<DataState<Boolean?>>()
    val favoriteState: LiveData<DataState<Boolean?>> get() = _favoriteState

    private var _restaurantState = MutableLiveData<DataState<Restaurant>>()
    val restaurantState: LiveData<DataState<Restaurant>> get() = _restaurantState

    private var _favorite = MutableLiveData<Boolean?>()
    val favorite: LiveData<Boolean?> get() = _favorite

    private var _currentRestaurant = MutableLiveData<Restaurant?>()
    val currentRestaurant: LiveData<Restaurant?> get() = _currentRestaurant
    fun addToFavorites(restaurantName: String) {
        viewModelScope.launch {
            repo.addToFavorite(restaurantName).collect {
                _addToFavoritesState.value = it

            }
        }
    }

    init {
        _favorite.value = null
        _currentRestaurant.value = null
    }


    fun getRestaurantFavoriteState(restaurantId: String) {
        viewModelScope.launch {
            repo.getRestaurantFavoriteState(restaurantId).collect {
                _favoriteState.value = it
            }
        }
    }

    fun getRestaurantById(restaurantId: String) {
        viewModelScope.launch {
            repo.getRestaurantById(restaurantId).collect {
                _restaurantState.value = it
            }
        }
    }

    fun setCurrentRestaurant(data: Restaurant) {
        _currentRestaurant.value = data
    }

    fun setFavoriteState(b: Boolean) {
        _favorite.value = b
    }

    fun onDoneObservingAddToFavoriteState() {
        _addToFavoritesState.value = null

    }
}
