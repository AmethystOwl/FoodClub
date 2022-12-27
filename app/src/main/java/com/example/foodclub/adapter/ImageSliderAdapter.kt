package com.example.foodclub.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.foodclub.databinding.SliderLayoutBinding
import com.smarteist.autoimageslider.SliderViewAdapter


class ImageSliderAdapter(private var mSliderItems: ArrayList<String>) :
    SliderViewAdapter<ImageSliderAdapter.SliderAdapterVH>() {

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        return SliderAdapterVH(
            SliderLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        viewHolder.bind(mSliderItems[position])
    }

    override fun getCount(): Int {
        return mSliderItems.size
    }

    inner class SliderAdapterVH(val binding: SliderLayoutBinding) :
        ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.imageUrl = item
        }
    }

}
