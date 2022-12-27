package com.example.foodclub.restaurant

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodclub.model.Restaurant

class RestaurantFragmentViewPagerAdapter(fa: FragmentActivity, private val restaurantId: String, private val restaurant: Restaurant) :
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RestaurantMenuFragment(restaurantId, restaurant.name!!,restaurant.iconImageUrl!!)
            1 -> RestaurantInfoFragment(restaurant.location!!, restaurant.workingHours!!)
            2 -> RestaurantReviewsFragment(restaurantId)
            else -> RestaurantMenuFragment(restaurantId, restaurant.name!!,restaurant.iconImageUrl!!)
        }
    }

}
