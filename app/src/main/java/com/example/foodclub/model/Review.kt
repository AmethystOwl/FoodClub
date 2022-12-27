package com.example.foodclub.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Review(
    var userId: String?,
    var userName: String?,
    var userImageUrl: String?,
    var rating: Float?,
    var review: String?,
    var timestamp: Timestamp? = Timestamp(Date())
) : Parcelable {
    constructor() : this(null, null, null, null, null, null)
}
