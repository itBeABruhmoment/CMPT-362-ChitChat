package com.example.cmpt_362_chitchat.ui.home.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ChatRoomListAdapter(
    val context: Context,
    private var resourceId: Int,
    private var chatrooms: ArrayList<String>,
    private var textPreviews: ArrayList<String>): BaseAdapter()  {

    override fun getCount(): Int {
        return chatrooms.size
    }

    override fun getItem(index: Int): Any {
        return chatrooms[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, p1: View?, p2: ViewGroup?): View {
        val view: View = View.inflate(context, resourceId,null)
        val chatroomName = view.findViewById<TextView>(android.R.id.text1)
        val chatroomPreview = view.findViewById<TextView>(android.R.id.text2)
        chatroomName.text = chatrooms[index]
        if (index < textPreviews.size) {
            chatroomPreview.text = textPreviews[index]
        }
        return view
    }

    fun replaceChatRooms(newChatrooms: ArrayList<String>) {
        chatrooms = newChatrooms
    }

    fun replaceTextPreviews(newPreviews: ArrayList<String>) {
        textPreviews = newPreviews
    }
}