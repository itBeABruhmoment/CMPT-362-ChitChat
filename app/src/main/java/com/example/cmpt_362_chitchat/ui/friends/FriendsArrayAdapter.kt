package com.example.cmpt_362_chitchat.ui.friends

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cmpt_362_chitchat.R

class FriendsArrayAdapter(var friends: ArrayList<User>, var context: Context) : BaseAdapter(){
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
        val view: View = View.inflate(context, R.layout.fragment_manage_friends_item, null)
        view.isClickable = false
        view.findViewById<TextView>(R.id.fragment_manage_friends_item_name).text = friends[position].name
        return view
    }
}