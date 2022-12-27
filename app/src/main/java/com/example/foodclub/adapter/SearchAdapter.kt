package com.example.foodclub.adapter

import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.android.highlighting.toSpannedString
import com.example.foodclub.R
import com.example.foodclub.databinding.ItemSearchBinding
import com.example.foodclub.model.SearchItem

class SearchAdapter(private val onSearchItemClickListener: OnSearchItemClickListener) :
    PagingDataAdapter<SearchItem, SearchAdapter.ProductViewHolder>(ProductDiffUtil) {

    class ProductViewHolder(private val binding: ItemSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ProductViewHolder {
                return ProductViewHolder(
                    ItemSearchBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        fun bind(searchItem: SearchItem, onSearchItemClickListener: OnSearchItemClickListener) {
            binding.onSearchItemClickListener = onSearchItemClickListener
            binding.searchItem = searchItem
            binding.productName.text = searchItem.highlightedTitle?.toSpannedString(
                ForegroundColorSpan(
                    binding.root.context.getColor(
                        R.color.primary
                    )
                )
            ) ?: searchItem.name
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position)!!, onSearchItemClickListener)
    }

    class OnSearchItemClickListener(private val onClick: (SearchItem) -> Unit) {
        fun onCLick(searchItem: SearchItem) = onClick(searchItem)
    }

    object ProductDiffUtil : DiffUtil.ItemCallback<SearchItem>() {
        override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem) =
            oldItem.objectID == newItem.objectID
                    && oldItem.restaurantId == newItem.restaurantId

        override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem) =
            oldItem == newItem
    }
}
