package com.example.cmpt_362_chitchat.ui.friends

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cmpt_362_chitchat.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FriendsActivity : AppCompatActivity(){
    private lateinit var viewPager: ViewPager2
    private lateinit var tabs: TabLayout
    private lateinit var manageFriendsFragment: ManageFriendsFragment
    private lateinit var viewFriendRequestsFragment: ViewFriendRequestsFragment
    private lateinit var tabFragments: ArrayList<Fragment>
    private lateinit var friendsActivityStateAdapter: FriendsActivityStateAdapter
    private lateinit var tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy
    private val tabNames: Array<String> = arrayOf("Friends", "Requests")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        viewPager = findViewById<ViewPager2>(R.id.activity_friends_view_pager)
        tabs = findViewById<TabLayout>(R.id.activity_friends_tabs)

        initFragments()

        friendsActivityStateAdapter = FriendsActivityStateAdapter(this)
        friendsActivityStateAdapter.fragments = tabFragments
        viewPager.adapter = friendsActivityStateAdapter
        tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
            tab.text = tabNames[position] }
        val tabLayoutMediator = TabLayoutMediator(tabs, viewPager, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }

    private fun initFragments() {
        manageFriendsFragment = ManageFriendsFragment()
        viewFriendRequestsFragment = ViewFriendRequestsFragment()
        tabFragments = arrayListOf(manageFriendsFragment, viewFriendRequestsFragment)
    }

    // adapter for action tabs
    inner class FriendsActivityStateAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        var fragments: ArrayList<Fragment> = ArrayList()

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }

        override fun getItemCount(): Int {
            return fragments.size
        }
    }
}