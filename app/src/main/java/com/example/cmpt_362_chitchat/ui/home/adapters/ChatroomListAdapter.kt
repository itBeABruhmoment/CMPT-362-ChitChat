package com.example.cmpt_362_chitchat.ui.home.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ChatroomListAdapter(
    val context: Context,
    private var resourceId: Int,
    private val items: Array<Array<String>>): BaseAdapter()  {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(index: Int): Any {
        return items[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, p1: View?, p2: ViewGroup?): View {
        val view: View = View.inflate(context, resourceId,null)
        val chatroomName = view.findViewById<TextView>(android.R.id.text1)
        val chatroomPreview = view.findViewById<TextView>(android.R.id.text2)
        chatroomName.text = items[index][0]
        chatroomPreview.text = items[index][1]
        return view
    }
}