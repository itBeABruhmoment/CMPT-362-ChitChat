package com.example.cmpt_362_chitchat.ui.chatRoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatRoomViewModel(chatRoom: String, private val chatRoomType: String) : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    var participantsListener: ValueEventListener
    val participants = MutableLiveData(HashMap<String, String>())

    init {
        participantsListener = database
            .child("ChatRooms")
            .child(chatRoomType)
            .child(chatRoom)
            .child("Participants")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newParticipantIds = ArrayList<String>()

                    for (snap in snapshot.children) {
                        newParticipantIds.add(snap.key.toString())
                    }

                    updateUsernames(newParticipantIds)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun updateUsernames(participantIds: ArrayList<String>) {
        val taskList1 = mutableListOf<Task<DataSnapshot>>()

        for (participant in participantIds) {
            val dbTask = database
                .child("Users")
                .child(participant)
                .get()
            taskList1.add(dbTask)
        }

        val resultTask1 = Tasks.whenAll(taskList1)
        resultTask1.addOnCompleteListener {
            val test = HashMap<String, String>()
            for (task in taskList1) {
                val snapshotKey: String? = task.result.key

                for (snap in task.result.children) {
                    if (snap.key == "username" && snapshotKey != null) {
                        test[snapshotKey] = snap.value.toString()
                    }
                }
            }
            participants.value = test
        }
    }

    fun getParticipantIds(): HashMap<String, String>? {
        return participants.value
    }
}