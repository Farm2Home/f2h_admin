package com.f2h.f2h_admin.screens.group.group_tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.f2h.f2h_admin.screens.group.all_items.AllItemsFragment
import com.f2h.f2h_admin.screens.group.members.MembersFragment
import com.f2h.f2h_admin.screens.group.group_wallet.GroupWalletFragment


class GroupDetailsTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        when(position) {
            0 -> {
                val fragment = AllItemsFragment()
                return fragment
            }

            1 -> {
                val fragment = MembersFragment()
                return fragment
            }

        }

        val fragment = AllItemsFragment()
        return fragment
    }
}
