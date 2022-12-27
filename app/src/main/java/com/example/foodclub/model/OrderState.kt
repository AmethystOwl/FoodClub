package com.example.foodclub.model

enum class OrderState(val value:String) {
    PLACED("Order placed"),
    ACCEPTED("Order accepted"),
    REJECTED("Order rejected"),
    CANCELED("Order canceled"),
    DELIVERED("Order delivered"),
    OUT_FOR_DELIVERY("Order's out for delivery"),

}
