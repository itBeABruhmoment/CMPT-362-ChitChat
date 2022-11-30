package com.example.cmpt_362_chitchat.ui.home.ui.privateChatRoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cmpt_362_chitchat.data.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class PrivateChatRoomViewModel : ViewModel() {
    private var database: DatabaseReference
    private val chatRoomListeners: HashMap<String, ValueEventListener> = HashMap()
    val chatRoomIds = MutableLiveData(ArrayList<String>())
    val chatRoomNames = MutableLiveData(ArrayList<String>())
    val chatRoomPreviews = MutableLiveData(ArrayList<String>())
    val currentPreviews = ArrayList<ArrayList<String>>()

    init {
        val userId = Firebase.auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().reference

        database.child("Users")
            .child(userId)
            .child("ChatRooms")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newChatRoomIds = ArrayList<String>()

                    for (snap in snapshot.children) {
                        newChatRoomIds.add(snap.key.toString())
                    }

                    updateChatRoomListeners(newChatRoomIds)
                    updateChatRooms(newChatRoomIds)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun updateChatRooms(newChatRoomIds: ArrayList<String>) {
        val newChatRoomNames = ArrayList<String>()
        // Get names from ChatRooms database
        FirebaseDatabase.getInstance().reference
            .child("ChatRooms")
            .child("Private")
            .get().addOnSuccessListener {
                for (chatRoomId in newChatRoomIds) {
                    newChatRoomNames.add(
                        it
                            .child(chatRoomId)
                            .child("ChatRoomName")
                            .value.toString()
                    )
                }

                // Update only after retrieving task from database is complete
                chatRoomIds.value = newChatRoomIds
                chatRoomNames.value = newChatRoomNames

            }
        
        updateTextPreviews(newChatRoomIds)
    }

    fun updateChatRoomListeners(chatRoomIds: ArrayList<String>) {
        for (chatRoomId in chatRoomIds) {
            if (chatRoomListeners[chatRoomId] == null) {
                val chatRoomListener = database
                    .child("ChatRooms")
                    .child("Private")
                    .child(chatRoomId)
                    .child("messages")
                    .limitToLast(1)
                    .addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val newPreview = ArrayList<String>()
                            newPreview.add(chatRoomId)

                            if (snapshot.childrenCount > 0) {
                                for (snap in snapshot.children) {
                                    val message = snap.getValue(Message::class.java)
                                    if (message != null) {
                                        newPreview.add(message.message.toString())
                                    }
                                }
                            } else {
                                newPreview.add("")
                            }

                            addToPreviewsList(newPreview, chatRoomIds)
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                chatRoomListeners[chatRoomId] = chatRoomListener
            }
        }

        // remove unnecessary chatroom listeners and text previews
        chatRoomListeners.forEach { entry ->
            if (!chatRoomIds.contains(entry.key)) {
                database
                    .child("ChatRooms")
                    .child("Private")
                    .child(entry.key)
                    .child("messages")
                    .removeEventListener(entry.value)

                removeFromPreviewsList(entry.key, chatRoomIds)
            }
        }
    }

    fun updateTextPreviews(chatRoomIds: ArrayList<String>) {
        val newPreviews = ArrayList<String>()

        for (chatRoomId in chatRoomIds) {
            for (preview in currentPreviews) {
                if (preview[0] == chatRoomId) {
                    newPreviews.add(preview[1])
                }
            }
        }

        chatRoomPreviews.value = newPreviews
    }

    fun addToPreviewsList(newPreview: ArrayList<String>, chatRoomIds: ArrayList<String>) {
        var previewExists = false
        for (preview in currentPreviews) {
            if (preview[0] == newPreview[0]) {
                preview[1] = newPreview[1]
                previewExists = true
            }
        }

        if (!previewExists) {
            currentPreviews.add(newPreview)
        }

        updateTextPreviews(chatRoomIds)
    }

    fun removeFromPreviewsList(chatRoomId: String, chatRoomIds: ArrayList<String>) {
        for (preview in currentPreviews) {
            if (preview[0] == chatRoomId) {
                currentPreviews.remove(preview)
                return
            }
        }

        updateTextPreviews(chatRoomIds)
    }

    fun getChatroom(index: Int) : String {
        return chatRoomIds.value?.get(index) ?: ""
    }
}