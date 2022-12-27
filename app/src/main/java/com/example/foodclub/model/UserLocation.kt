package com.example.foodclub.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserLocation(
    var lat: Double?,
    var long: Double?,
    var street: String?,
    var building: Int?,
    var floor: Int?,
    var apartment: Int?
) : Parcelable {
    constructor() :  this(null,null,null,null,null,null)
}
