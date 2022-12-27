package com.example.foodclub.register

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.UserProfile
import com.example.foodclub.shared.Repository
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private val _registerState = MutableLiveData<DataState<UserProfile>?>()
    val registerState: LiveData<DataState<UserProfile>?> get() = _registerState

    fun registerUser(user: UserProfile, password: String) {
        viewModelScope.launch {
            repo.register(user, password).collect {
                _registerState.value = it
            }
        }
    }


    private val _uploadImageState = MutableLiveData<DataState<Int>>()
    val uploadImageState: LiveData<DataState<Int>> get() = _uploadImageState


    fun uploadProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            repo.uploadProfilePicture(imageUri).collect {
                _uploadImageState.value = it
            }
        }

    }
}
