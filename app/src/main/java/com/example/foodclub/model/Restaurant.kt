package com.example.foodclub.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Restaurant(
    @get:Exclude var id: String?,
    var name: String?,
    var rating: Double? = 0.0,
    var noOfReviews: Long? = 0,
    var location: String?,
    var iconImageUrl: String? = null,
    var cuisines: ArrayList<String>?,
    var foodPreviewList: ArrayList<String>?,
    var workingHours: String?
) : Parcelable {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}
