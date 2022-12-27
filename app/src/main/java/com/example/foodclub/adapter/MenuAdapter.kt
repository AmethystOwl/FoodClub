package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.R
import com.example.foodclub.databinding.MenuItemBinding
import com.example.foodclub.model.CartItemUi
import com.example.foodclub.model.MenuItemUi


class MenuAdapter(
    private val onMenuItemClickListener: OnMenuItemClickListener,
    private val cartItems: ArrayList<CartItemUi>
) :
    ListAdapter<MenuItemUi, MenuAdapter.ViewHolder>(MenuItemsDiffUtil()) {
    class ViewHolder(val binding: MenuItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            model: MenuItemUi,
            onMenuItemClickListener: OnMenuItemClickListener,
            cartItems: ArrayList<CartItemUi>
        ) {

            val present = cartItems.filter { it.mealId == model.id }
            if (present.isNotEmpty() && present.size == 1) {
                model.present = true
                model.qty = present[0].quantity?.toInt()!!
            }

            binding.menuItem = model
            binding.onMenuItemClickListener = onMenuItemClickListener

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                return ViewHolder(
                    MenuItemBinding.bind(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.menu_item, parent, false)
                    )
                )
            }
        }
    }

    class OnMenuItemClickListener(private val onClickListener: (menuItemUi: MenuItemUi) -> Unit) {
        fun onClick(menuItemUi: MenuItemUi) = onClickListener(menuItemUi)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onMenuItemClickListener, cartItems)
    }

    private class MenuItemsDiffUtil : DiffUtil.ItemCallback<MenuItemUi>() {
        override fun areItemsTheSame(oldItem: MenuItemUi, newItem: MenuItemUi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MenuItemUi, newItem: MenuItemUi): Boolean {
            return oldItem == newItem
        }

    }


}

