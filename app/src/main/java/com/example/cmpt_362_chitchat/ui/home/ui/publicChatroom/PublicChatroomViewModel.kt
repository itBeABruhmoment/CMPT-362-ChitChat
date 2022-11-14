package com.example.cmpt_362_chitchat.ui.home.ui.publicChatroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PublicChatroomViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the public chatroom Fragment"
    }
    val text: LiveData<String> = _text
}