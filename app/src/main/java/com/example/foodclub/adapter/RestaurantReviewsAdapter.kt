package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.databinding.ItemReviewBinding
import com.example.foodclub.model.Review
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class RestaurantReviewsAdapter(options: FirestoreRecyclerOptions<Review>) :
    FirestoreRecyclerAdapter<Review, RestaurantReviewsAdapter.ViewHolder>(options) {
    class ViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }

        fun bind(review: Review) {
            binding.review = review
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Review) {
        holder.bind(model)
    }
}
