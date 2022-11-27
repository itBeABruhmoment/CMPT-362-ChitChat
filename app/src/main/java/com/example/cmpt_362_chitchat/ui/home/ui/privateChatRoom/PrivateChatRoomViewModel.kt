package com.example.cmpt_362_chitchat.ui.home.ui.privateChatRoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class PrivateChatRoomViewModel : ViewModel() {

    init {
        val userId = Firebase.auth.currentUser?.uid.toString()

        val database = FirebaseDatabase.getInstance().reference
        database.child("Users")
            .child(userId)
            .child("ChatRooms")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newChatrooms = ArrayList<String>()

                    for (snap in snapshot.children) {
                        newChatrooms.add(snap.key.toString())
                    }

                    updateChatrooms(newChatrooms)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    val chatrooms = MutableLiveData(ArrayList<String>())

    fun updateChatrooms(newChatrooms: ArrayList<String>) {
        chatrooms.value = newChatrooms
    }

    fun getChatroom(index: Int) : String {
        return chatrooms.value?.get(index) ?: ""
    }
}