
package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.R
import com.example.foodclub.databinding.CategoryLayoutBinding
import com.example.foodclub.model.CartItemUi
import com.example.foodclub.model.MenuCategoryUi
import com.example.foodclub.model.MenuItemUi

class MenuCategoryAdapter(
    private val onMenuItemClickListener: MenuAdapter.OnMenuItemClickListener,
    private val cartItems: ArrayList<CartItemUi>,
) : ListAdapter<MenuCategoryUi, MenuCategoryAdapter.ViewHolder>(CategoryDiffUtil()) {


    class ViewHolder(private val binding: CategoryLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.category_layout, parent, false)
                return ViewHolder(CategoryLayoutBinding.bind(view))
            }
        }

        fun bind(
            category: MenuCategoryUi,
            onMenuItemClickListener: MenuAdapter.OnMenuItemClickListener,
            menuItemUis: List<MenuItemUi>,
            cartItems : ArrayList<CartItemUi>
        ) {

            binding.categoryName = category.categoryName
            val menuAdapter = MenuAdapter(onMenuItemClickListener,cartItems)
            menuAdapter.submitList(menuItemUis)
            binding.menuItemsRecyclerView.adapter = menuAdapter
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),onMenuItemClickListener,getItem(position).menuItemUis?.toList()!!,cartItems)
    }


    private class CategoryDiffUtil : DiffUtil.ItemCallback<MenuCategoryUi>() {
        override fun areItemsTheSame(oldItem: MenuCategoryUi, newItem: MenuCategoryUi): Boolean {
            return oldItem.categoryName == newItem.categoryName
        }

        override fun areContentsTheSame(oldItem: MenuCategoryUi, newItem: MenuCategoryUi): Boolean {
            return oldItem == newItem
        }

    }

}

