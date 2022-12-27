package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.R
import com.example.foodclub.databinding.HomeRestaurantLayoutBinding
import com.example.foodclub.model.Restaurant
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RestaurantAdapter(
    options: FirestoreRecyclerOptions<Restaurant>,
    private val onClickCallback: OnRestaurantClickListener,
) : FirestoreRecyclerAdapter<Restaurant, RestaurantAdapter.ViewHolder>(options) {

    inner class ViewHolder(private val binding: HomeRestaurantLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(homeRestaurant: Restaurant, onClickCallback: OnRestaurantClickListener) {
            binding.homeRestaurant = homeRestaurant
            binding.onClickListener = onClickCallback
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HomeRestaurantLayoutBinding.bind(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.home_restaurant_layout, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Restaurant) {
        model.id = snapshots.getSnapshot(position).id
        holder.bind(model, onClickCallback)

    }

    class OnRestaurantClickListener(private val onClickListener: (String) -> Unit) {
        fun onClick(restaurantId: String) = onClickListener(restaurantId)
    }


}
