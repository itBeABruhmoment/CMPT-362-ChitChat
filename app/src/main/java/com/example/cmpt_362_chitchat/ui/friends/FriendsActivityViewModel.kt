package com.example.cmpt_362_chitchat.ui.friends

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.IllegalArgumentException

class FriendsActivityViewModel(private val user: FirebaseUser) : ViewModel() {
    private var database: DatabaseReference = Firebase.database.reference
    public val friendsRequests: MutableLiveData<ArrayList<String>> = MutableLiveData()
    public val friends: MutableLiveData<ArrayList<String>> = MutableLiveData()

    init {
        friendsRequests.value = ArrayList()
        friends.value = ArrayList()

        database.child("users")
            .child(user.uid)
            .child("friends")
            .addValueEventListener(FriendsPostListener())

        database.child("users")
            .child(user.uid)
            .child("friendRequests")
            .addValueEventListener(FriendsRequestPostListener())

    }

    private inner class FriendsRequestPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val requests: ArrayList<String> = ArrayList()
            snapshot.children.forEach() {
                val uid: String? = it.getValue(String::class.java)
                if(uid != null) {
                    requests.add(uid)
                } else {
                    Log.i("FriendsActivity", "uid of friend request null")
                }
            }
            this@FriendsActivityViewModel.friendsRequests.value = requests
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("FriendsActivity", "error with friend request data")
            Log.i("FriendsActivity", error.message)
        }
    }

    private inner class FriendsPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val friends: ArrayList<String> = ArrayList()
            snapshot.children.forEach() {
                val uid: String? = it.getValue(String::class.java)
                if(uid != null) {
                    friends.add(uid)
                } else {
                    Log.i("FriendsActivity", "uid of friend request null")
                }
            }
            this@FriendsActivityViewModel.friends.value = friends
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("FriendsActivity", "error with friends data")
            Log.i("FriendsActivity", error.message)
        }
    }
}

class FriendsActivityViewModelFactory (private val user: FirebaseUser) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(FriendsActivityViewModel::class.java))
            return FriendsActivityViewModel(user) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}