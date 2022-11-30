package com.example.cmpt_362_chitchat.ui.home.ui.publicChatRoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PublicChatRoomViewModel : ViewModel() {

    init {
        val database = FirebaseDatabase.getInstance().reference
        database
            .child("ChatRooms")
            .child("Public")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newChatRoomIDs = ArrayList<String>()
                    val newChatRoomNames = ArrayList<String>()

                    for(snap in snapshot.children) {
                        newChatRoomIDs.add(snap.key.toString())
                        newChatRoomNames.add(snap.child("ChatRoomName").value.toString())
                    }

                    updateChatRoomIDs(newChatRoomIDs, newChatRoomNames)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    val chatRoomIDs = MutableLiveData(ArrayList<String>())
    val chatRoomNames = MutableLiveData(ArrayList<String>())

    fun updateChatRoomIDs(newChatRoomIDs: ArrayList<String>, newChatRoomNames: ArrayList<String>) {
        chatRoomIDs.value = newChatRoomIDs
        chatRoomNames.value = newChatRoomNames
    }

    fun getChatroomID(index: Int) : String {
        return chatRoomIDs.value?.get(index) ?: ""
    }
}