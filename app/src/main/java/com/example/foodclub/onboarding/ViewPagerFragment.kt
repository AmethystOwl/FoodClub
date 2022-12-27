package com.example.foodclub.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.foodclub.activity.MainActivity
import com.example.foodclub.databinding.FragmentViewPagerBinding
import com.example.foodclub.onboarding.screens.FirstFragment
import com.example.foodclub.onboarding.screens.SecondFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as MainActivity).hideBottomNavigation()

        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)

        val fragmentList = arrayListOf(FirstFragment(), SecondFragment())
        binding.viewPager.adapter = ViewPagerAdapter(fragmentList, childFragmentManager, lifecycle)

        binding.indicator.setViewPager(binding.viewPager)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    1 -> binding.indicator.visibility = View.GONE
                    else -> binding.indicator.visibility = View.VISIBLE
                }
            }
        })

        return binding.root
    }


    private inner class ViewPagerAdapter(
        private val pages: ArrayList<Fragment>,
        fm: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fm, lifecycle) {
        override fun getItemCount() = pages.size
        override fun createFragment(position: Int) = pages[position]

    }
}
