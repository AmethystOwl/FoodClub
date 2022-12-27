package com.example.foodclub.home

import androidx.lifecycle.ViewModel
import com.example.foodclub.shared.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repository) : ViewModel() {


    fun getDefaultQuery(ordered: Boolean) =
        repo.getDefaultRestaurantsQuery(ordered)

    fun filterRestaurantsQuery(cuisineName: String) =
        repo.filterRestaurantsQuery(cuisineName)

    fun getFilteredQuery(checked: ArrayList<String>) =
        repo.getFilteredQuery(checked)


}
