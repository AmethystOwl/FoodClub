package com.example.foodclub.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodclub.model.UserLocation
import com.example.foodclub.model.UserProfile
import com.example.foodclub.utils.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(private val repo: Repository) : ViewModel() {
    private var _currentUserProfileState = MutableLiveData<DataState<UserProfile?>?>()
    val currentUserProfileState: LiveData<DataState<UserProfile?>?> get() = _currentUserProfileState

    private var _currentUserProfile = MutableLiveData<UserProfile?>()
    val currentUserProfile: LiveData<UserProfile?> get() = _currentUserProfile

    private var _userLocation = MutableLiveData<UserLocation?>()
    val userLocation: LiveData<UserLocation?> get() = _userLocation

    private var _passwordState = MutableLiveData<DataState<Int>>()
    val passwordState: LiveData<DataState<Int>> get() = _passwordState
    fun fetchCurrentUserProfile() {
        if (repo.isUserSignedIn()) {
            viewModelScope.launch {
                repo.getCurrentUserProfile().collect {
                    _currentUserProfileState.value = it
                }
            }
        } else {
            _currentUserProfileState.value = null
        }
    }

    fun setCurrentUserProfile(userProfile: UserProfile?) {
        if (repo.isUserSignedIn()) {
            _currentUserProfile.value = userProfile
            _userLocation.value = userProfile?.location
        } else {
            _currentUserProfile.value = null
            _userLocation.value = null
        }

    }

    fun logOut() {
        _currentUserProfile.value = null
        _userLocation.value = null
        repo.logOut()

    }

    private var _locationState = MutableLiveData<DataState<UserLocation>>()
    val locationState: LiveData<DataState<UserLocation>> get() = _locationState
    fun updateLocation(userLocation: UserLocation) {
        viewModelScope.launch {
            repo.updateLocation(userLocation).collect {
                _locationState.value = it
            }
        }
    }

    fun onDoneObservingLocation() {
        _locationState.value = DataState.Empty
    }

    fun setUserLocation(location: UserLocation) {
        _userLocation.value = location
    }

    fun changePassword(curPass: String, newPass: String) {
        viewModelScope.launch {
            repo.changePassword(curPass, newPass).collect {
                _passwordState.value = it
            }
        }
    }

    fun onDoneObservingPassword() {
        _passwordState.value = DataState.Empty
    }
}
