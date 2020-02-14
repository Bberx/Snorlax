package com.snorlax.snorlax.utils.adapter.viewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.snorlax.snorlax.ui.home.generate.GeneratorClassFragment
import com.snorlax.snorlax.ui.home.generate.GeneratorManualFragment

class GeneratorAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                GeneratorClassFragment()
            }
            1 -> {
                GeneratorManualFragment()
            }
            else -> throw IllegalStateException("Invalid page position")
        }
    }


}