package com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class NewChatRoomViewModel : ViewModel() {
    private var database: DatabaseReference
    val friendIds = MutableLiveData(ArrayList<String>())
    val friendUsernames = MutableLiveData(ArrayList<String>())

    init {
        val userId = Firebase.auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().reference

        database.child("Users")
            .child(userId)
            .child("friends")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentFriendIds = ArrayList<String>()
                    val currentFriendUsernames = ArrayList<String>()
                    val taskList = mutableListOf<Task<DataSnapshot>>()

                    for (snap in snapshot.children) {
                        currentFriendIds.add(snap.key.toString())

                        val databaseReferenceTask: Task<DataSnapshot> =
                            database
                                .child("Users")
                                .child(snap.key.toString())
                                .child("username")
                                .get()
                        taskList.add(databaseReferenceTask)
                    }

                    val resultTask = Tasks.whenAll(taskList)
                    resultTask.addOnCompleteListener {
                        for (task in taskList) {
                            currentFriendUsernames.add(task.result.value.toString())
                        }
                        friendIds.value = currentFriendIds
                        friendUsernames.value = currentFriendUsernames
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}