package com.example.foodclub.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class MenuItemPreferencesCategory(
    val title: String,
    val items: @RawValue ArrayList<MenuItemPreferences>
) : Parcelable
