package org.thoughtcrime.securesms.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Created by Yaakov on
 * Describe:
 */
class ManagerViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return ChatsFragment()
            1 -> return DaoFragment()
            2 -> return MeFragment()
        }
        return ChatsFragment()
    }

    override fun getItemCount(): Int {
        return 3
    }
}
