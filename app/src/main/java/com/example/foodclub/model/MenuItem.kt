package com.example.foodclub.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItem(
    @get:Exclude var id: String?,
    var name: String?,
    var description: String?,
    var price: Double?,
    var likes: Long?,
    var imageUrl: String?,
    var category: String?
) : Parcelable {
    constructor() : this(null, null, null, null, null, null, null)
}
