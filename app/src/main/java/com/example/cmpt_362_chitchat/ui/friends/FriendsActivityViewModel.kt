package com.example.cmpt_362_chitchat.ui.friends

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cmpt_362_chitchat.data.CombinedQuery
import com.example.cmpt_362_chitchat.data.CombinedWrite
import com.example.cmpt_362_chitchat.data.SingularQuery
import com.example.cmpt_362_chitchat.data.SingularWrite
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
import java.lang.IllegalArgumentException
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.CopyOnWriteArrayList

class FriendsActivityViewModel(private val user: FirebaseUser) : ViewModel() {
    // for storing info needed to display users
    private var database: DatabaseReference = Firebase.database.reference
    private val friendRequestQueue: SendFriendRequestQueue = SendFriendRequestQueue()
    public val friendsRequests: MutableLiveData<ArrayList<FriendRequestEntry>> = MutableLiveData()
    public val sentRequests: MutableLiveData<ArrayList<FriendRequestEntry>> = MutableLiveData()
    public val friends: MutableLiveData<ArrayList<FriendEntry>> = MutableLiveData()

    init {
        friendsRequests.value = ArrayList()
        friends.value = ArrayList()

        getFriendsNode(user.uid).addValueEventListener(FriendsPostListener())
        getFriendRequestsNode(user.uid).addValueEventListener(FriendsRequestPostListener())
        getSentRequestsNode(user.uid).addValueEventListener(SentRequestPostListener())
    }

    public fun addFriendRequest(sender: String, recipient: String): AddFriendRequestResult {
        friendRequestQueue.addRequest(sender, recipient)
        return AddFriendRequestResult.SUCCESS
    }

    public fun removeFriendRequest(friendRequest: FriendRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            getSentRequestsNode(friendRequest.sender)
                .child(friendRequest.id)
                .removeValue().addOnFailureListener {
                    Log.i(
                        "FriendsActivity",
                        "failed to delete sent request ${friendRequest.id} from ${friendRequest.sender}"
                    )
                }

            getFriendRequestsNode(friendRequest.recipient)
                .child(friendRequest.id)
                .removeValue().addOnFailureListener {
                    Log.i(
                        "FriendsActivity",
                        "failed to delete sent request ${friendRequest.id} from ${friendRequest.recipient}"
                    )
                }
        }
    }

    public fun addFriend(friendRequest: FriendRequest) {
        getFriendsNode(friendRequest.sender)
            .child(friendRequest.recipient)
            .setValue(true)
            .addOnFailureListener {
                Log.i(
                    "FriendsActivity",
                    "failed to add ${friendRequest.recipient} as friend"
                )
            }

        getFriendsNode(friendRequest.recipient)
            .child(friendRequest.sender)
            .setValue(true).addOnFailureListener {
                Log.i(
                    "FriendsActivity",
                    "failed to add ${friendRequest.sender} as friend"
                )
            }
    }

    public fun removeFriend(friend: String) {
        getFriendsNode(friend).removeValue().addOnFailureListener() {
            Log.i("FriendsActivity", "failed to add remove friend $friend from user")
        }

        getFriendsNode(user.uid).removeValue().addOnFailureListener() {
            Log.i("FriendsActivity", "failed to remove user as friend of $friend")
        }
    }

    private fun getFriendsNode(uid: String): DatabaseReference {
        return database.child("Users").child(user.uid).child(FRIENDS)
    }

    private fun getFriendRequestsNode(uid: String): DatabaseReference {
        return database.child("Users").child(user.uid).child(RECIEVED_REQUESTS)
    }

    private fun getSentRequestsNode(uid: String): DatabaseReference {
        return database.child("Users").child(uid).child(SENT_REQUESTS)
    }

    private fun requestExists(otherUser: String): Boolean {
        val sentRequests: ArrayList<FriendRequestEntry>? = sentRequests.value
        val receivedRequests: ArrayList<FriendRequestEntry>? = friendsRequests.value

        if(sentRequests != null) {
            for(sentRequest: FriendRequestEntry in sentRequests) {
                if(sentRequest.request.sender == user.uid
                    && sentRequest.request.recipient == otherUser) {
                    return true
                }
            }
        }
        if(receivedRequests != null) {
            for(receivedRequest: FriendRequestEntry in receivedRequests) {
                if(receivedRequest.request.sender == otherUser
                    && receivedRequest.request.recipient == user.uid) {
                    return true
                }
            }
        }
        return false
    }

    private fun friendExists(otherUser: String): Boolean {
        val friendsTemp: ArrayList<FriendEntry>? = friends.value
        if(friendsTemp != null) {
            for(friend: FriendEntry in friendsTemp) {
                if(friend.uid == otherUser) {
                    return true
                }
            }
        }
        return false
    }

    private fun allowFriendRequest(attemptToSend: FriendRequest, callBack: (allow: Boolean) -> Unit) {
        var isAlreadyFriend: Boolean = false;
        var isAlreadySent: Boolean = false;
        var isAlreadyBeingAsked: Boolean = false;

        val queryFriends: SingularQuery = SingularQuery(
            getFriendsNode(attemptToSend.sender),
            { friends ->
                isAlreadyFriend = friends != null && friends.hasChild(attemptToSend.recipient)
            },
            {
                Log.i("allowFriendRequest()", it.message.toString())
            }
        )
        val querySentRequests: SingularQuery = SingularQuery(
            getSentRequestsNode(attemptToSend.sender),
            { sentRequestsSnapshot ->
                if(sentRequestsSnapshot != null) {
                    sentRequestsSnapshot.children.forEach { sentRequestSnapshot ->
                        val request: FriendRequest? = sentRequestSnapshot.getValue(FriendRequest::class.java)
                        if(request != null && request.recipient == attemptToSend.recipient) {
                            isAlreadySent = true;
                        }
                    }
                }
            },
            {
                Log.i("allowFriendRequest()", it.message.toString())
            }
        )
        val queryReceivedRequests: SingularQuery = SingularQuery(
            getFriendRequestsNode(attemptToSend.sender),
            { receivedRequestsSnapshot ->
                if (receivedRequestsSnapshot != null) {
                    receivedRequestsSnapshot.children.forEach { receivedRequestSnapshot ->
                        val request: FriendRequest? = receivedRequestSnapshot.getValue(FriendRequest::class.java)
                        if(request != null && request.recipient == attemptToSend.sender) {
                            isAlreadyBeingAsked = true;
                        }
                    }
                }
            },
            {
                Log.i("allowFriendRequest()", it.message.toString())
            }
        )

        val queries: CombinedQuery = CombinedQuery(arrayListOf(queryFriends, queryReceivedRequests, querySentRequests)) {
            Log.i("allowFriendRequest()", "$isAlreadyFriend $isAlreadySent $isAlreadyBeingAsked")
            callBack(!isAlreadyFriend && !isAlreadySent && !isAlreadyBeingAsked)
        }
    }

    private fun getUserInfo(
        users: ArrayList<String>,
        onComplete: (ArrayList<UserProfile>, Boolean) -> Unit
    ) {
        val userInfo: ArrayList<UserProfile> = ArrayList(users.size)
        val queries: ArrayList<SingularQuery> = ArrayList(users.size)
        for(i in 0 until users.size) {
            userInfo.add(UserProfile(""))
            val query: SingularQuery = SingularQuery(
                database.child("Users").child(users[i]).child("username"),
                { dataSnapshot ->
                    Log.i("FriendsActivity", "got user $i")
                    val temp: String? = dataSnapshot?.getValue(String::class.java)
                    if(temp == null) {
                        Log.i("FriendsActivity", "user null")
                    } else {
                        userInfo[i].userName = temp
                    }
                },
                {
                    Log.i("FriendsActivity", "failed to get user")
                }
            )
            queries.add(query)
        }

        CombinedQuery(queries) {
            onComplete(userInfo, it.size != 0)
        }
    }


    private inner class RequestDataPostListener(
        val onComplete: (ArrayList<FriendRequest>, Boolean) -> Unit
    ) : ValueEventListener {
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
            onComplete(requests, true)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("FriendsActivity", "error with friend request data")
            Log.i("FriendsActivity", error.message)
            onComplete(ArrayList(), false)
        }
    }



    private inner class FriendsRequestPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.i("FriendsRequestPostListener", "friend request onDataChange")
            val requests: ArrayList<FriendRequest> = ArrayList()
            snapshot.children.forEach() {
                val request: FriendRequest? = it.getValue(FriendRequest::class.java)
                if(request != null) {
                    requests.add(request)
                } else {
                    Log.i("FriendsRequestPostListener", "uid of friend request null")
                }
            }

            Log.i("FriendsRequestPostListener", "${requests.size} requests")
            val profilesToGet: ArrayList<String> = ArrayList()
            for(request: FriendRequest in requests) {
                profilesToGet.add(request.sender)
            }

            getUserInfo(profilesToGet) { profiles, failed ->
                if(!failed) {
                    Log.i("FriendsRequestPostListener", "${profiles.size} ####################")
                    val requestEntries: ArrayList<FriendRequestEntry> = ArrayList(profiles.size)
                    for(i in 0 until profiles.size) {
                        requestEntries.add(FriendRequestEntry(profiles[i].userName, requests[i]))
                    }
                    friendsRequests.value = requestEntries
                } else {
                    Log.i("FriendsRequestPostListener", "failed#################")
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("FriendsActivity", "error with friend request data")
            Log.i("FriendsActivity", error.message)
        }
    }

    private inner class SentRequestPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.i("FriendsActivity", "sent request onDataChange")
            val requests: ArrayList<FriendRequest> = ArrayList()
            snapshot.children.forEach() {
                val request: FriendRequest? = it.getValue(FriendRequest::class.java)
                if(request != null) {
                    requests.add(request)
                } else {
                    Log.i("FriendsActivity", "uid of sent request null")
                }
            }

            Log.i("FriendsActivity", "${requests.size} sent requests")
            val profilesToGet: ArrayList<String> = ArrayList()
            for(request: FriendRequest in requests) {
                profilesToGet.add(request.recipient)
            }
            val queryUsers: GroupedUserQuery = GroupedUserQuery(profilesToGet) { profiles, failed ->
                if(failed) {
                    Log.i("FriendsActivity", "failed to get all users")
                } else {
                    Log.i("FriendsActivity", "got all users")
                    val requestEntries: ArrayList<FriendRequestEntry> = ArrayList(profiles.size)
                    for(i in 0 until profiles.size) {
                        requestEntries.add(FriendRequestEntry(profiles[i].userName, requests[i]))
                    }
                    sentRequests.value = requestEntries
                }
            }
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
                val uid: String? = it.key
                if(uid != null) {
                    friends.add(uid)
                } else {
                    Log.i("FriendsActivity", "uid of friend request null")
                }
            }

            val getFriendsProfiles: GroupedUserQuery = GroupedUserQuery(friends) { profiles, failed ->
                if(failed) {
                    Log.i("FriendsActivity", "failed to get profiles of friends")
                } else {
                    val friendEntries: ArrayList<FriendEntry> = ArrayList(profiles.size)
                    for(i in 0 until profiles.size) {
                        friendEntries.add(FriendEntry(profiles[i].userName, friends[i]))
                    }
                    this@FriendsActivityViewModel.friends.value = friendEntries
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("FriendsActivity", "error with friends data")
            Log.i("FriendsActivity", error.message)
        }
    }

    // friend request sending has to be done sequentially to ensure no redundant requests are sent
    // queue for operations to send friend requests
    private inner class SendFriendRequestQueue {
        private val sendRequestQueue: Queue<FriendRequest> = LinkedList()

        @Synchronized
        public fun addRequest(sender: String, recipient: String) {
            Log.i("flag", "1")
            // make request
            val request: FriendRequest = FriendRequest("", sender, recipient)
            val senderAddNode: DatabaseReference = getSentRequestsNode(sender).push()
            val key: String? = senderAddNode.key
            if(key == null) {
                Log.i("Friends", "failed to construct request")
                return
            }
            request.id = key

            var length: Int = 0
            doQueueOperation { queue ->
                length = queue.size
            }

            Log.i("flag", "2")
            if(length == 0) { // empty queue, write right away
                Log.i("flag", "3a")
                Log.i("Friends", "friend request queue empty")
                tryToAddFriend(request)
            } else { // add to queue to wait for processing
                Log.i("flag", "3b")
                Log.i("Friends", "friend request queue not empty")
                doQueueOperation { queue ->
                    queue.add(request)
                }
            }

        }

        // tries to add a friend to database
        // after a friend is added, this function will be called recursively to process next request in queue if any
        private fun tryToAddFriend(request: FriendRequest?) {
            Log.i("Friends", "tryToAddFriend()")
            if(request == null) {
                return
            }

            Log.i("flag", "4a")
            allowFriendRequest(request) { allow ->
                Log.i("flag", "5a")
                if(allow) {
                    // add friendship on database
                    val toSender: SingularWrite = SingularWrite(
                        request,
                        getSentRequestsNode(request.sender).child(request.id),
                        {
                            Log.i("Friends", "added request to sender")
                        },
                        {
                            Log.i("Friends", "failed to add request to sender")
                        }
                    )
                    val toRecipient: SingularWrite = SingularWrite (
                        request,
                        getFriendRequestsNode(request.recipient).child(request.id),
                        {
                            Log.i("Friends", "added request to recipient")
                        },
                        {
                            Log.i("Friends", "failed to add request to recipient")
                        }
                    )
                    val writes: ArrayList<SingularWrite> = arrayListOf(toSender, toRecipient)
                    // write to database and run callback when done
                    val combinedWrite: CombinedWrite = CombinedWrite(writes) {
                        var toProcess: FriendRequest? = null
                        doQueueOperation {
                            if(it.size > 0) {
                                toProcess = it.remove()
                            }
                        }
                        tryToAddFriend(toProcess)
                    }
                }
            }
        }

        // don't interleave operations that edit queue
        @Synchronized
        private fun doQueueOperation(run: (queue: Queue<FriendRequest>) -> Unit) {
            run(sendRequestQueue)
        }
    }

    // allow for multiple user's data to be queried with a single callback
    private inner class GroupedUserQuery {
        private lateinit var received: ArrayList<UserProfile>
        private var entriesAcquired: Int = 0
        private var failed: Boolean = false
        private lateinit var onComplete: (ArrayList<UserProfile>, Boolean) -> Unit

        private constructor() { }

        constructor(users: ArrayList<String>, onComplete: (ArrayList<UserProfile>, Boolean) -> Unit) {
            Log.i("FriendsActivity", "GroupedUserQuery constructor")
            this.onComplete = onComplete

            if(users.size == 0) {
                onComplete(ArrayList(), false)
            }

            // init received
            val placeHolder: UserProfile = UserProfile("")
            received = ArrayList(users.size)
            for(i in 0 until users.size) {
                received.add(placeHolder)
            }

            viewModelScope.launch(Dispatchers.IO) {
                for(i in 0 until users.size) {
                    val tempUid: String = users[i] // I have no idea how thread safe for loops are
                    val index: Int = i
                    val userName = database.child("Users").child(tempUid).child("username").get()

                    userName.addOnSuccessListener {
                        val name: String? = it.getValue(String::class.java)
                        if(name != null) {
                            Log.i("FriendsActivity", "got name $name")
                            addReceived(index, UserProfile(name))
                        }
                    }.addOnFailureListener {
                        setFailed()
                        addReceived(index, UserProfile(""))
                        Log.i("FriendsActivity", "failed to get username of ${tempUid}")
                    }.addOnCanceledListener {
                        setFailed()
                        addReceived(index, UserProfile(""))
                        Log.i("FriendsActivity", "failed to get username of ${tempUid}")
                    }
                }
            }
        }

        @Synchronized
        private fun addReceived(index: Int, userData: UserProfile) {
            received[index] = userData
            entriesAcquired++
            Log.i("FriendsActivity", "$this: $entriesAcquired/${received.size}")

            // all queries done
            if(entriesAcquired == received.size) {
                onComplete(received, failed)
            }
        }

        @Synchronized
        private fun setFailed() {
            failed = true
        }
    }

    companion object {
        val SENT_REQUESTS = "sent"
        val RECIEVED_REQUESTS = "recieved"
        val FRIENDS = "friends"

        class UserProfile {
            public lateinit var userName: String

            constructor(userName: String) {
                this.userName = userName
            }
        }

        class FriendEntry(var useName: String, var uid: String) {

        }

        class FriendRequestEntry {
            public lateinit var userName: String
            public lateinit var request: FriendRequest

            constructor() {
                this.userName = ""
                this.request = FriendRequest()
            }

            constructor(userName: String) {
                this.userName = userName
                this.request = FriendRequest()
            }

            constructor(userName: String, request: FriendRequest) {
                this.userName = userName
                this.request = request
            }
        }

        enum class AddFriendRequestResult {
            SUCCESS,
            CANT_VERIFY_NON_DUPLICATE,
            ALREADY_FRIEND,
            DUPLICATE_REQUEST
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