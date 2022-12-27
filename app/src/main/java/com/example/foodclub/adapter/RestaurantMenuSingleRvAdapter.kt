/*package com.example.foodclub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodclub.databinding.CategoryLayoutBinding
import com.example.foodclub.databinding.MenuItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PainAdapter :
    ListAdapter<PainAdapter.DataItem, RecyclerView.ViewHolder>(MenuItemDiff()) {
    private val adapterScope = CoroutineScope(Dispatchers.IO)

    companion object {
         const val VIEW_TYPE_ITEM = 1
         const val VIEW_TYPE_SECTION_HEADER = 2
    }

    fun addHeaderAndSubmitList(list: List<Test>) {
        adapterScope.launch {
            val groupedList = list.groupBy { it.category }
            val myList = ArrayList<DataItem>()
            for (i in groupedList.keys) {
                myList.add(DataItem.Header(i!!))
                for (v in groupedList.getValue(i)) {
                    myList.add(DataItem.Item(v))
                }
            }

            withContext(Dispatchers.Main) {
                submitList(myList)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> ItemViewHolder.from(parent)
            VIEW_TYPE_SECTION_HEADER -> CategoryViewHolder.from(parent)
            else -> throw IllegalArgumentException("Invalid ViewHolder")
        }
    }

    private class CategoryViewHolder(private val binding: CategoryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): CategoryViewHolder {
                return CategoryViewHolder(
                    CategoryLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }

        fun bind(categoryName: String) {
            binding.categoryName = categoryName
            binding.executePendingBindings()
        }

    }

    private class ItemViewHolder(private val binding: MenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                return ItemViewHolder(
                    MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
        }

        fun bind(item: Test) {
            binding.menuItem = item
            binding.executePendingBindings()

        }
    }


    sealed class DataItem {
        data class Item(val product: Test) : DataItem() {
            override val id = product.id!!
            override val className = "Item"
        }

        data class Header(val typeName: String) : DataItem() {
            override val id = typeName.hashCode().toString()
            override val className = "Header"

        }

        abstract val id: String
        abstract val className: String

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = getItem(position) as DataItem.Item
                holder.bind(item.product)
            }
            is CategoryViewHolder -> {
                val header = getItem(position) as DataItem.Header
                holder.bind(header.typeName)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> VIEW_TYPE_SECTION_HEADER
            is DataItem.Item -> VIEW_TYPE_ITEM
        }
    }
}

private class MenuItemDiff : DiffUtil.ItemCallback<PainAdapter.DataItem>() {
    override fun areItemsTheSame(
        oldItem: PainAdapter.DataItem,
        newItem: PainAdapter.DataItem
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: PainAdapter.DataItem,
        newItem: PainAdapter.DataItem
    ): Boolean {
        return oldItem == newItem
    }

}*/