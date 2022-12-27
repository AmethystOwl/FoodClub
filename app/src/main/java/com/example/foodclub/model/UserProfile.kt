package com.example.foodclub.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize


@Parcelize
data class UserProfile(
    @get:Exclude  var uId: String?,
    val isUser: Boolean = true,
    var name: String,
    val email: String,
    var dateOfBirth: String,
    var phoneNumber: String,
    var isEmailVerified: Boolean = false,
    var isPhoneVerified: Boolean = false,
    var profilePictureUrl: String? = null,
    var favorites: ArrayList<String>?= ArrayList(),
    var location: UserLocation?
) : Parcelable {
    constructor() : this(
        null,
        true,
        "",
        "",
        "",
        "",
        false,
        false,
        null,
        ArrayList(),
        null
    )
}
