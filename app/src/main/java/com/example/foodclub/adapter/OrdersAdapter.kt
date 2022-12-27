package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.databinding.ItemOrderBinding
import com.example.foodclub.model.OrderItemUi

class OrdersAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<OrderItemUi, OrdersAdapter.ViewHolder>(OrdersDiffUtil()) {

    class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderItemUi, onClickListener: OnClickListener) {
            binding.orderItem = item
            binding.onClickListener = onClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    ItemOrderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
    }

    class OnClickListener(private val onClickListener: (orderItem: OrderItemUi) -> Unit) {
        fun onClick(orderItem: OrderItemUi) = onClickListener(orderItem)

    }

    private class OrdersDiffUtil : DiffUtil.ItemCallback<OrderItemUi>() {
        override fun areItemsTheSame(oldItem: OrderItemUi, newItem: OrderItemUi): Boolean {
            return oldItem.mealId == newItem.mealId && oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderItemUi, newItem: OrderItemUi): Boolean {
            return oldItem == newItem
        }

    }
}
