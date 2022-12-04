package com.example.cmpt_362_chitchat.ui.friends

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.IllegalArgumentException
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class FriendsActivityViewModel(private val user: FirebaseUser) : ViewModel() {
    // for storing info needed to display users
    private var database: DatabaseReference = Firebase.database.reference
    //private val friendRequestQueue: SendFriendRequestQueue = SendFriendRequestQueue()
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
        val request: FriendRequest = FriendRequest("", sender, recipient)
        val senderAddNode: DatabaseReference = getSentRequestsNode(sender).push()
        val key: String? = senderAddNode.key
        if(key == null) {
            Log.i("addRequest()", "failed to construct request")
            return AddFriendRequestResult.DUPLICATE_REQUEST
        }
        request.id = key

        runBlocking {
            withContext(Dispatchers.IO) {
                allowFriendRequest(request) { allow ->
                    if(allow) {
                        Log.i("tryToAddFriend", "allow true")
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

                        }
                    }
                }
            }
        }

        //friendRequestQueue.addRequest(sender, recipient)
        //friendRequestQueue.addRequest(sender, recipient)
        //friendRequestQueue.addRequest(sender, recipient)
        return AddFriendRequestResult.SUCCESS
    }

    /*
    // friend request sending has to be done sequentially to ensure no redundant requests are sent
    // queue for operations to send friend requests
    private inner class SendFriendRequestQueue {
        private val sendRequestQueue: CopyOnWriteArrayList<FriendRequest> = CopyOnWriteArrayList()
        private val processingRequest: AtomicBoolean = AtomicBoolean(false)

        @Synchronized
        public fun addRequest(sender: String, recipient: String) {
            // make request
            val request: FriendRequest = FriendRequest("", sender, recipient)
            val senderAddNode: DatabaseReference = getSentRequestsNode(sender).push()
            val key: String? = senderAddNode.key
            if(key == null) {
                Log.i("addRequest()", "failed to construct request")
                return
            }
            request.id = key

            var length: Int = sendRequestQueue.size
            sendRequestQueue.add(request)

            if(length == 0) { // queue was previously empty, start processing
                Log.i("addRequest()", "friend request queue empty")
                tryToAddFriend()
            } else {
                Log.i("addRequest()", "not empty")
            }
        }

        // tries to add a friend to database
        // after a friend is added, this function will be called recursively to process next request in queue if any
        private fun tryToAddFriend() {
            Log.i("tryToAddFriend()", "start")
            var requestTemp: FriendRequest? = null; // TODO: try using copy on write queue
            if(sendRequestQueue.size != 0) {
                requestTemp = sendRequestQueue.get(0)
            }

            Log.i("tryToAddFriend()", "flag 1")
            val request = requestTemp
            if(request == null) {
                Log.i("tryToAddFriend()", "request null")
                processingRequest.set(false)
                return
            }

            Log.i("tryToAddFriend()", "flag 2")
            Log.i("tryToAddFriend()", "request ${request.id}")
            allowFriendRequest(request) { allow ->
                if(allow) {
                    Log.i("tryToAddFriend", "allow true")
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
                        Log.i("tryToAddFriend()", "next")
                        sendRequestQueue.removeAt(0)
                        Log.i("tryToAddFriend()", "${sendRequestQueue.size} left")
                        tryToAddFriend()
                    }
                }
            }
        }
    }

     */

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

    //load photo from database, from profile activity
    public fun loadPhoto(uid : String, imageView: ImageView) {
        val storageRef = FirebaseStorage.getInstance().reference.child("UserPhotos/$uid")
        //create a temp location for photo
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            //update userPhoto
            imageView.setImageBitmap(bitmap)
            println("DEBUG: photo successfully loaded")
        }.addOnFailureListener {
            println("DEBUG: photo was not able to load")
        }
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

    private suspend fun allowFriendRequest(attemptToSend: FriendRequest, callBack: (allow: Boolean) -> Unit) {
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
            callBack(!(isAlreadyFriend || isAlreadySent || isAlreadyBeingAsked))
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