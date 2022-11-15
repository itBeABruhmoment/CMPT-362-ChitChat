package com.example.cmpt_362_chitchat.ui.friends

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.cmpt_362_chitchat.R

class FriendRequestArrayAdapter(var requests: ArrayList<FriendRequest>, var context: Context) : BaseAdapter(){
    override fun getCount(): Int {
        return requests.size
    }

    override fun getItem(index: Int): Any {
        return requests[index]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = View.inflate(context, R.layout.fragment_friend_request_item, null)
        view.isClickable = false
        view.findViewById<TextView>(R.id.fragment_friend_request_item_name).text = requests[position].name
        return view
    }
}