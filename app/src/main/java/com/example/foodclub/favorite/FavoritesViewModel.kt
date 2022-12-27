package com.example.foodclub.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.Restaurant
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class FavoritesViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private var _favoritesState = MutableLiveData<DataState<ArrayList<Restaurant>>?>()
    val favoritesState: LiveData<DataState<ArrayList<Restaurant>>?> get() = _favoritesState

    private var _removeRestaurantState = MutableLiveData<DataState<Boolean>>()
    val removeRestaurantState: LiveData<DataState<Boolean>> get() = _removeRestaurantState

    fun getFavorites() {
        viewModelScope.launch {
            repo.defaultFavoritesQuery().collect {
                _favoritesState.value = it
            }
        }
    }

    fun removeRestaurant(id: String) {
        viewModelScope.launch {
            repo.removeRestaurant(id).collect {
                _removeRestaurantState.value = it
            }
        }

    }
}
