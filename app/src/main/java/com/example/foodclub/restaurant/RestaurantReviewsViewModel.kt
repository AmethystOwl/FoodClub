package com.example.foodclub.restaurant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.Review
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantReviewsViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private var _reviewState = MutableLiveData<DataState<Int>>()
    val reviewState: LiveData<DataState<Int>> get() = _reviewState

    fun postReview(restaurantId: String, review: Review) {
        viewModelScope.launch {
            repo.postReview(restaurantId, review).collect {
                _reviewState.value = it
            }
        }
    }

    fun getReviewsQuery(restaurantId: String): Query {
        return repo.getReviewsQuery(restaurantId)
    }

}
