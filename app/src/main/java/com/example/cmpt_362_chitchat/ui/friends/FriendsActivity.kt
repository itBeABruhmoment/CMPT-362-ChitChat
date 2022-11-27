package com.example.cmpt_362_chitchat.ui.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.ui.login.LoginActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FriendsActivity : AppCompatActivity(){
    private lateinit var viewModel: FriendsActivityViewModel

    private lateinit var viewPager: ViewPager2
    private lateinit var tabs: TabLayout
    private lateinit var manageFriendsFragment: ManageFriendsFragment
    private lateinit var viewFriendRequestsFragment: ViewFriendRequestsFragment
    private lateinit var tabFragments: ArrayList<Fragment>
    private val tabNames: Array<String> = arrayOf("Friends", "Requests")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        // get user or refer user to login screen if null

        val tempUser: FirebaseUser? = Firebase.auth.currentUser
        if(tempUser == null) {
            Log.i("FriendsActivity", "user null, going to login page")
            val intent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            Log.i("FriendsActivity", "user not null, continue")
            val viewModelFactory: FriendsActivityViewModelFactory = FriendsActivityViewModelFactory(tempUser)
            viewModel = ViewModelProvider(this, viewModelFactory).get(FriendsActivityViewModel::class.java)
        }

        viewPager = findViewById<ViewPager2>(R.id.activity_friends_view_pager)
        tabs = findViewById<TabLayout>(R.id.activity_friends_tabs)

        initFragments()

        val friendsActivityStateAdapter = FriendsActivityStateAdapter(this)
        friendsActivityStateAdapter.fragments = tabFragments
        viewPager.adapter = friendsActivityStateAdapter
        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
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