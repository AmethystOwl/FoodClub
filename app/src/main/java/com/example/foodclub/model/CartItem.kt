package com.example.foodclub.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val restaurantId: String?,
    val mealId: String?,
    val quantity: Long?,
) : Parcelable {
    constructor() : this(
        null,
        null,
        null
    )
}

@Parcelize
data class CartItemUi(
    val mealId: String?,
    val quantity: Long?,
    val name: String?,
    val price: Double?,
    val total: Double? = (price!! * quantity?.toDouble()!!),
) : Parcelable {
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
    )
}

@Parcelize
data class CartUi(
    val restaurant: Restaurant?,
    val items: ArrayList<CartItemUi>?
) : Parcelable {
    constructor() : this(
        null,
        null,
        )
}
