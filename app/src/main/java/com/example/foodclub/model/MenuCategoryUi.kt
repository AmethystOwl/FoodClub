package com.example.foodclub.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class MenuCategoryUi(
    var categoryName: String?,
    var menuItemUis: @RawValue List<MenuItemUi>?
) : Parcelable{
    constructor() : this(null,null)
}
