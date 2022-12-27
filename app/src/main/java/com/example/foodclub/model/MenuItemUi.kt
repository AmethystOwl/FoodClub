package com.example.foodclub.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItemUi(
    @get:Exclude var id: String?,
    var name: String?,
    var description: String?,
    var price: Double?,
    var likes: Long?,
    var imageUrl: String?,
    @get:Exclude var present: Boolean? = false,
    @get:Exclude var qty: Int? = 0,
) : Parcelable {
    constructor() : this(null, null, null, null, null, null, null, null)

}
