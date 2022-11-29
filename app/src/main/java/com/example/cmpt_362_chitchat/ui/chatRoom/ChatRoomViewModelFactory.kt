package com.example.cmpt_362_chitchat.ui.chatRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatRoomViewModelFactory
    (private val chatRoomId: String, private val chatRoomType: String) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create (modelClass: Class<T>) : T{
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java))
            return ChatRoomViewModel(chatRoomId, chatRoomType) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}