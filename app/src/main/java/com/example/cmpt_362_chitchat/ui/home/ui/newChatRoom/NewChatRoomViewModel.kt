package com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NewChatRoomViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the new chatroom Fragment"
    }
    val text: LiveData<String> = _text
}