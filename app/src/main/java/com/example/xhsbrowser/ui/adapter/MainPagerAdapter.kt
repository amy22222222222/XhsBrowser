package com.example.xhsbrowser.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.xhsbrowser.ui.fragments.DashboardFragment
import com.example.xhsbrowser.ui.fragments.HistoryFragment
import com.example.xhsbrowser.ui.fragments.SettingsFragment
import com.example.xhsbrowser.ui.fragments.StatsFragment

class MainPagerAdapter(activity: androidx.fragment.app.FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> DashboardFragment()
        1 -> HistoryFragment()
        2 -> StatsFragment()
        3 -> SettingsFragment()
        else -> DashboardFragment()
    }
}
