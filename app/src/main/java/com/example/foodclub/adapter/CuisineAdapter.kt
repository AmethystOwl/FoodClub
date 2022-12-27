package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.R
import com.example.foodclub.databinding.ItemCuisineBinding
import com.example.foodclub.model.Cuisine

class CuisineAdapter(
    private val items: ArrayList<Cuisine>,
    private val onCuisineClickListener: OnCuisineClickListener
) : RecyclerView.Adapter<CuisineAdapter.ViewHolder>() {

    private var lastSelectedPos = 0

    class ViewHolder(private val binding: ItemCuisineBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.item_cuisine, parent, false)
                return ViewHolder(ItemCuisineBinding.bind(view))
            }
        }

        fun bind(cuisine: Cuisine, onCuisineClickListener: OnCuisineClickListener, position: Int) {
            binding.cuisine = cuisine
            binding.onCuisineClickListener = onCuisineClickListener
            binding.position = position
            binding.executePendingBindings()

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(items[position], onCuisineClickListener, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class OnCuisineClickListener(private val onClickListener: (cuisine: Cuisine, position: Int) -> Unit) {
        fun onClick(cuisine: Cuisine, position: Int) = onClickListener(cuisine, position)
    }

    fun selectItem(position: Int):Boolean {
        if (position == lastSelectedPos) return false
        items[lastSelectedPos].isSelected = false
        notifyItemChanged(lastSelectedPos)
        lastSelectedPos = position
        items[lastSelectedPos].isSelected = true
        notifyItemChanged(position)
        return true
    }
}
