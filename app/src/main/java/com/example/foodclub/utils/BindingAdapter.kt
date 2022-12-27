package com.example.foodclub.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.foodclub.R
import com.example.foodclub.adapter.ImageSliderAdapter
import com.example.foodclub.model.OrderState
import com.example.foodclub.model.UserLocation
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Timestamp
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import java.util.*
import java.util.stream.IntStream.range


@BindingAdapter("bindCuisines")
fun TextView.bindCuisines(cuisines: List<String>?) {
    cuisines?.let {
        val stringBuilder = StringBuilder("")
        range(0, cuisines.size).forEach { cuisineIndex ->
            if (cuisineIndex == cuisines.size - 1) {
                stringBuilder.append(cuisines[cuisineIndex])
            } else {
                stringBuilder.append(cuisines[cuisineIndex] + ", ")
            }
        }
        text = stringBuilder
    }

}

@BindingAdapter("bindFoodPreviewImages")
fun bindFoodPreviewImages(sliderView: SliderView, imageUrls: ArrayList<String>?) {
    imageUrls?.let {
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM)
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
        sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_RIGHT
        sliderView.indicatorSelectedColor = Color.WHITE
        sliderView.indicatorUnselectedColor = Color.GRAY
        sliderView.scrollTimeInSec = 4
        sliderView.startAutoCycle()
        sliderView.setSliderAdapter(ImageSliderAdapter(it))
    }


}



@BindingAdapter("bindImage")
fun ImageView.bindImage(imageUrl: String?) {
    Glide.with(context)
        .load(imageUrl)
        .placeholder(R.drawable.giphy)
        .fitCenter()
        .into(this)
}

@BindingAdapter("bindRating")
fun TextView.bindRating(rating: Long) {
    text = this.context.getString(R.string.ratings_template, rating.toString())
}

@BindingAdapter("categorySelector")
fun CardView.categorySelector(isSelected: Boolean) {
    when (isSelected) {
        true -> {
            setCardBackgroundColor(context.getColor(R.color.primary))
        }
        false -> {
            setCardBackgroundColor(context.getColor(R.color.transparent))
        }
    }


}


@BindingAdapter("bindQuantity")
fun TextView.bindQuantity(value: Int) {
    if (value > 0) {
        text = value.toString()
        setTextColor(context.getColor(R.color.red))
    } else {
        text = context.getString(R.string.plus_symbol)
        setTextColor(context.getColor(R.color.black))

    }

}

@BindingAdapter("bindStrokeWidth")
fun MaterialCardView.bindStrokeWidth(value: Boolean) {
    strokeWidth = when (value) {
        true -> 2
        false -> 0
    }
}

@BindingAdapter("bindAddButtonText")
fun MaterialButton.bindAddButtonText(present: Boolean) {
    text = when (present) {
        false -> context.getString(R.string.add_to_cart)
        true -> context.getString(R.string.edit)
    }
}

@BindingAdapter("bindQuantityTextView")
fun TextView.bindQuantityTextView(qty: Int?) {
    text = when (qty) {
        null -> "1"
        0 -> "1"
        else -> qty.toString()
    }
}

@BindingAdapter(value = ["timeStamp", "state"], requireAll = true)
fun MaterialTextView.bindOrderDate(timestamp: Timestamp, state: OrderState) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp.seconds * 1000L
    val date = DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString()
    text = context.getString(R.string.orderState, state.value, date)

}

@BindingAdapter("bindOrderRestaurant")
fun MaterialTextView.bindOrderRestaurant(restaurantName: String) {
    text = context.getString(R.string.orderedFrom, restaurantName)
}

@BindingAdapter("bindAddress")
fun TextView.bindAddress(userLocation: UserLocation?) {
    text = userLocation?.let {
        context.getString(
            R.string.userLocation,
            userLocation.apartment,
            userLocation.floor,
            userLocation.building,
            userLocation.street
        )
    } ?: "Unknown"
}

@BindingAdapter("registerLocation")
fun TextView.registerLocation(userLocation: UserLocation?) {
    text = userLocation?.let {
        context.getString(
            R.string.userLocation,
            userLocation.apartment,
            userLocation.floor,
            userLocation.building,
            userLocation.street
        )
    } ?: "Your Location"
}


@BindingAdapter("bindBitmap")
fun ImageView.bindBitmap(imageBitmap: Bitmap?) {
    imageBitmap?.let {
        Glide.with(context).load(imageBitmap).into(this)
    }
}
