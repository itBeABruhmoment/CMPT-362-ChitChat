package com.example.cmpt_362_chitchat.ui.home.ui.privateChatroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PrivateChatroomViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the private chatroom Fragment"
    }
    val text: LiveData<String> = _text
}