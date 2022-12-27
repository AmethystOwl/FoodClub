package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.R
import com.example.foodclub.databinding.ItemFavoriteBinding
import com.example.foodclub.model.Restaurant


class FavoritesAdapter(
    private val items: ArrayList<Restaurant>,
    private val onClickCallback: OnFavoritesItemClickListener,
    private val onRemoveCallback: OnRemoveItemClickListener
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    class FavoritesViewHolder(private val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): FavoritesViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.item_favorite, parent, false)
                return FavoritesViewHolder(ItemFavoriteBinding.bind(view))
            }
        }

        fun bind(
            restaurant: Restaurant,
            onFavoritesItemClickListener: OnFavoritesItemClickListener,
            onRemoveCallback: OnRemoveItemClickListener,
            position: Int
        ) {
            binding.restaurant = restaurant
            binding.onClickListener = onFavoritesItemClickListener
            binding.onRemoveItemClickListener = onRemoveCallback
            binding.position = position
            binding.executePendingBindings()

        }
    }


    class OnFavoritesItemClickListener(private val onClickListener: (String) -> Unit) {
        fun onClick(restaurantId: String) = onClickListener(restaurantId)
    }

    class OnRemoveItemClickListener(private val onClickListener: (String, Int) -> Unit) {
        fun onClick(restaurantId: String, position: Int) = onClickListener(restaurantId, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        holder.bind(items[position], onClickCallback, onRemoveCallback, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        return FavoritesViewHolder.from(parent)
    }
    fun removeItem(position: Int){
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}
