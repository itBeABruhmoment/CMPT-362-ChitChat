package com.example.cmpt_362_chitchat.ui.friends

import android.app.DownloadManager.Request
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cmpt_362_chitchat.data.model.FriendRequest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class FriendsActivityViewModel(private val user: FirebaseUser) : ViewModel() {
    private var database: DatabaseReference = Firebase.database.reference
    public val friendsRequests: MutableLiveData<ArrayList<FriendRequest>> = MutableLiveData()
    public val friends: MutableLiveData<ArrayList<String>> = MutableLiveData()

    init {
        friendsRequests.value = ArrayList()
        friends.value = ArrayList()

        database.child("Users")
            .child(user.uid)
            .child("friends")
            .addValueEventListener(FriendsPostListener())

        database.child("Users")
            .child(user.uid)
            .child(RECIEVED_REQUESTS)
            .addValueEventListener(FriendsRequestPostListener())

    }

    public fun addFriendRequest(sender: String, recipient: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val addLocation: DatabaseReference = database
                .child("Users")
                .child(sender)
                .child(SENT_REQUESTS)
                .push()

            val toAdd: FriendRequest = FriendRequest("", sender, recipient)
            val key: String? = addLocation.key
            if(key != null) {
                toAdd.id = key
                // to recipient
                database
                    .child("Users")
                    .child(recipient)
                    .child(RECIEVED_REQUESTS)
                    .child(toAdd.id)
                    .setValue(toAdd)
                // to sender
                addLocation.setValue(toAdd)
            } else {
                Log.i("FriendsActivity", "no key")
            }
        }
    }

    public fun removeFriendRequest(friendRequest: FriendRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            database
                .child("Users")
                .child(friendRequest.sender)
                .child(SENT_REQUESTS)
                .child(friendRequest.id)
                .removeValue().addOnFailureListener {
                    Log.i(
                        "FriendsActivity",
                        "failed to delete sent request ${friendRequest.id} from ${friendRequest.sender}"
                    )
                }

            database
                .child("Users")
                .child(friendRequest.recipient)
                .child(SENT_REQUESTS)
                .child(friendRequest.id)
                .removeValue().addOnFailureListener {
                    Log.i(
                        "FriendsActivity",
                        "failed to delete sent request ${friendRequest.id} from ${friendRequest.recipient}"
                    )
                }
        }
    }

    private inner class FriendsRequestPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.i("FriendsActivity", "friend request onDataChange")
            val requests: ArrayList<FriendRequest> = ArrayList()
            snapshot.children.forEach() {
                val request: FriendRequest? = it.getValue(FriendRequest::class.java)
                if(request != null) {
                    requests.add(request)
                } else {
                    Log.i("FriendsActivity", "uid of friend request null")
                }
            }

            val handler = Handler(Looper.getMainLooper())
            for(request: FriendRequest in requests) {
                viewModelScope.launch(Dispatchers.IO) {
                    val data: FriendRequest = request

                    val userName = database.child("Users").child(data.sender).child("username").get()

                    userName.addOnSuccessListener {
                        val name: String? = it.getValue(String::class.java)

                        // update livedata
                        handler.post() {
                            if(name != null) {
                                Log.i("FriendsActivity", "got name $name")
                            }
                        }
                    }.addOnFailureListener {
                        Log.i("FriendsActivity", "failed to get username of ${data.sender}")
                    }
                }
            }

            friendsRequests.value = requests
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("FriendsActivity", "error with friend request data")
            Log.i("FriendsActivity", error.message)
        }
    }

    private inner class FriendsPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.i("FriendsActivity", "friend onDataChange")
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

    companion object {
        val SENT_REQUESTS = "sent"
        val RECIEVED_REQUESTS = "recieved"

        class FriendRequestData {
            lateinit var uid: String
            lateinit var username: String

            private constructor() {}

            constructor(uid: String, userName: String) {
                this@FriendRequestData.uid = uid
                this@FriendRequestData.username = username
            }
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