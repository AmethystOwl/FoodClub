package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.R
import com.example.foodclub.databinding.ItemCartBinding
import com.example.foodclub.model.CartItemUi


class CartAdapter(
    private val onCartItemRemoveClickListener: OnCartItemRemoveClickListener,
    private val onCartItemEditClickListener: OnCartItemEditClickListener
) :
    ListAdapter<CartItemUi, CartAdapter.ViewHolder>(CartDiffUtil()) {
    class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            model: CartItemUi,
            onCartItemRemoveClickListener: OnCartItemRemoveClickListener,
            onCartItemEditClickListener: OnCartItemEditClickListener,
            position: Int,
        ) {
            binding.cartItemUi = model
            binding.onCartItemRemoveClickListener = onCartItemRemoveClickListener
            binding.onCartItemEditClickListener = onCartItemEditClickListener
            binding.position = position
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    ItemCartBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_cart, parent, false)
                    )
                )
            }

        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            getItem(position),
            onCartItemRemoveClickListener,
            onCartItemEditClickListener,
            position,
        )
        holder.binding.deleteItemMaterialButton.setOnClickListener {
            if (holder.absoluteAdapterPosition != -1) {
                onCartItemRemoveClickListener.onRemove(
                    getItem(holder.absoluteAdapterPosition),
                    holder.absoluteAdapterPosition
                )
            }

        }
    }


    class OnCartItemRemoveClickListener(val onRemoveListener: (CartItemUi, Int) -> Unit) {
        fun onRemove(cartItem: CartItemUi, position: Int) =
            onRemoveListener(cartItem, position)
    }

    class OnCartItemEditClickListener(val onEditListener: (CartItemUi, Int) -> Unit) {
        fun onEdit(cartItem: CartItemUi, position: Int) = onEditListener(cartItem, position)
    }

    private class CartDiffUtil : DiffUtil.ItemCallback<CartItemUi>() {
        override fun areItemsTheSame(oldItem: CartItemUi, newItem: CartItemUi): Boolean =
            oldItem.mealId == newItem.mealId


        override fun areContentsTheSame(oldItem: CartItemUi, newItem: CartItemUi): Boolean =
            oldItem == newItem

    }
}
