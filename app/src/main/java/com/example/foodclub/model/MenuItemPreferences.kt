package com.example.foodclub.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItemPreferences(val name:String, val price:Double):Parcelable
