package com.example.foodclub.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.type.LatLng
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Parcelize
data class OrderItemUi(
    val orderId: String?,
    val restaurant: Restaurant?,
    val mealId: String?,
    val quantity: Long?,
    val name: String?,
    val imageUrl: String?,
    val price: Double?,
    val total: Double?,
    val state: OrderState? = OrderState.PLACED,
    val timestamp: Timestamp?,
) : Parcelable {
    constructor() : this(null, null, null,null, null, null, null, null, null, null)
}

@Parcelize
data class OrderUi(
    val orderId: String?,
    val restaurant: Restaurant?,
    val items: ArrayList<OrderItemUi>?,
    val imageUrl: String?,
    val address: String?,
    val latLng: @RawValue LatLng?,
    val timestamp: Timestamp?,
    val paymentMethod: OrderPaymentMethod?,
) : Parcelable {
    constructor() : this(null, null, null, null, null, null, null, null)
}

@Parcelize
data class OrderItem(
    @get:Exclude var orderId: String?,
    var userId: String?,
    val restaurantId: String?,
    val items: @RawValue ArrayList<OrderItemsDetails>?,
    val timestamp: Timestamp? = Timestamp(Date()),
    val state: OrderState? = OrderState.PLACED,
    val paymentMethod: OrderPaymentMethod? = OrderPaymentMethod.CASH_ON_DELIVERY,
) : Parcelable {
    constructor() : this(null, null, null, null, null, null, null)
}

@Parcelize
data class OrderItemsDetails(
    val mealId: String?,
    val price: Double?,
    val quantity: Long?,
    val total: Double? = quantity?.toDouble()!! * price!!,
) : Parcelable {
    constructor() : this(null, null, null, null)
}