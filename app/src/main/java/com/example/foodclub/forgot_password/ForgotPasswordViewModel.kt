package com.example.foodclub.forgot_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private var _resetState = MutableLiveData<DataState<Int>>()
    val resetState: LiveData<DataState<Int>> get() = _resetState

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            repo.sendResetEmail(email).collect {
                _resetState.value = it
            }
        }
    }

    fun onDoneObservingResetState() {
        _resetState.value = DataState.Empty
    }

}