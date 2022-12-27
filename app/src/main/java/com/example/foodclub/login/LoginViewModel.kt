package com.example.foodclub.login

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
class LoginViewModel @Inject constructor(private val repo: Repository) : ViewModel() {

    private val _loginState = MutableLiveData<DataState<*>>()
    val loginState: LiveData<DataState<*>> get() = _loginState

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    fun login(email: String, password: String) {
        viewModelScope.launch {
            repo.login(email, password).collect {
                _loginState.value = it
            }
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun onDoneSettingError() {
        _errorMessage.value = null
    }
}
