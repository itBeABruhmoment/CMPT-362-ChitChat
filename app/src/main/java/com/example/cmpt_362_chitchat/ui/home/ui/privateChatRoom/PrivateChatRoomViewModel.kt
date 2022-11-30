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
                    val newChatRoomIDs = ArrayList<String>()

                    for (snap in snapshot.children) {
                        newChatRoomIDs.add(snap.key.toString())
                    }

                    updateChatRoomIDs(newChatRoomIDs)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    val chatRoomIDs = MutableLiveData(ArrayList<String>())
    val chatRoomNames = MutableLiveData(ArrayList<String>())

    private fun updateChatRoomIDs(newChatRoomIDs: ArrayList<String>) {
        val newChatRoomNames = ArrayList<String>()
        // Get names from ChatRooms database
        FirebaseDatabase.getInstance().reference
            .child("ChatRooms")
            .child("Private")
            .get().addOnSuccessListener {
                for (chatRoomId in newChatRoomIDs) {
                    newChatRoomNames.add(
                        it
                            .child(chatRoomId)
                            .child("ChatRoomName")
                            .value.toString()
                    )
                }

                // Update only after retrieving task from database is complete
                chatRoomIDs.value = newChatRoomIDs
                chatRoomNames.value = newChatRoomNames

            }
    }

    fun getChatroom(index: Int) : String {
        return chatRoomIDs.value?.get(index) ?: ""
    }

}