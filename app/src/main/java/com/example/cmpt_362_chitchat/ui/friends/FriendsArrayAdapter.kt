package com.example.cmpt_362_chitchat.ui.friends

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.cmpt_362_chitchat.R

class FriendsArrayAdapter(
    var friends: ArrayList<FriendsActivityViewModel.Companion.FriendEntry>,
    var activity: Activity,
    var viewModel: FriendsActivityViewModel
    ) : BaseAdapter(){
    override fun getCount(): Int {
        return friends.size
    }

    override fun getItem(index: Int): Any {
        return friends[index]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(activity, R.layout.fragment_manage_friends_item, null)
        view.isClickable = false
        view.findViewById<TextView>(R.id.fragment_manage_friends_item_name).text = friends[position].useName

        // set up kebab menu
        val kebabMenu: ImageButton = view.findViewById(R.id.fragment_manage_friends_item_etc)
        kebabMenu.setOnClickListener {
            val popUp: PopupMenu = PopupMenu(activity, kebabMenu)
            popUp.menuInflater.inflate(R.menu.friend_kebab_menu, popUp.menu)
            popUp.setOnMenuItemClickListener() {
                val picked: Int = it.itemId
                if(picked == R.id.friend_kebab_menu_profile) {
                    Log.i("Friends Activity", "profile")
                } else if(picked == R.id.friend_kebab_menu_unfriend) {
                    Log.i("Friends Activity", "unfriend")
                    viewModel.removeFriend(friends[position].uid )
                }
                return@setOnMenuItemClickListener true
            }
            popUp.show()
        }
        return view
    }
}