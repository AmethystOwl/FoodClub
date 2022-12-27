package com.example.foodclub.location

import androidx.lifecycle.ViewModel
import com.example.foodclub.shared.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LocationViewModel @Inject constructor(private val repo: Repository) : ViewModel() {



}
