package com.example.cmpt_362_chitchat.ui.friends

import android.graphics.Bitmap
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
import kotlinx.coroutines.*
import java.io.File
import java.lang.IllegalArgumentException
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class FriendsActivityViewModel(private val user: FirebaseUser) : ViewModel() {
    // for storing info needed to display users
    private val profilePics: ConcurrentHashMap<String, Bitmap> = ConcurrentHashMap(50)

    private var database: DatabaseReference = Firebase.database.reference
    public val friendsRequests: MutableLiveData<ArrayList<FriendRequestEntry>> = MutableLiveData()
    public val sentRequests: MutableLiveData<ArrayList<FriendRequestEntry>> = MutableLiveData()
    public val friends: MutableLiveData<ArrayList<FriendEntry>> = MutableLiveData()

    init {
        friendsRequests.value = ArrayList()
        friends.value = ArrayList()

        // init database observers
        getFriendsNode(user.uid).addValueEventListener(FriendsPostListener())
        getFriendRequestsNode(user.uid).addValueEventListener(FriendsRequestPostListener())
        getSentRequestsNode(user.uid).addValueEventListener(SentRequestPostListener())
    }

    // check if a friend request is redundant and send it if it isn't
    @OptIn(ExperimentalCoroutinesApi::class)
    public fun addFriendRequest(sender: String, recipient: String) {
        // make FriendRequest object
        val request: FriendRequest = FriendRequest("", sender, recipient)
        val senderAddNode: DatabaseReference = getSentRequestsNode(sender).push()
        val key: String? = senderAddNode.key
        if(key == null) {
            Log.i("addRequest()", "failed to construct request")
            return
        }
        request.id = key

        // max one instance of this coroutine running at a time
        CoroutineScope(Dispatchers.IO.limitedParallelism(1)).launch {
            runBlocking {
                Log.i("addFriendRequest", "start")
                if (allowFriendRequest(request)) {
                    Log.i("addFriendRequest", "allow true")
                    // add friend request to database
                    runBlocking { writeFriendRequest(request) }
                }
                Log.i("addFriendRequest", "done")
            }
        }
        return
    }

    // remove friendRequest from database
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

    // add friend to database
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

    // remove friend from database
    public fun removeFriend(friend: String) {
        getFriendsNode(user.uid).child(friend).removeValue().addOnFailureListener() {
            Log.i("FriendsActivity", "failed to add remove friend $friend from user")
        }

        getFriendsNode(friend).child(user.uid).removeValue().addOnFailureListener() {
            Log.i("FriendsActivity", "failed to remove user as friend of $friend")
        }
    }

    private fun getFriendsNode(uid: String): DatabaseReference {
        return database.child("Users").child(uid).child(FRIENDS)
    }

    private fun getFriendRequestsNode(uid: String): DatabaseReference {
        return database.child("Users").child(uid).child(RECIEVED_REQUESTS)
    }

    private fun getSentRequestsNode(uid: String): DatabaseReference {
        return database.child("Users").child(uid).child(SENT_REQUESTS)
    }

    // load photo from database
    // based on code from profile activity
    public fun loadPhoto(uid : String, imageView: ImageView) {
        val alreadyExists: Bitmap? = profilePics.get(uid)
        if(alreadyExists != null) {
            imageView.setImageBitmap(alreadyExists)
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("UserPhotos/$uid")
        //create a temp location for photo
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile).addOnSuccessListener {
            CoroutineScope(Dispatchers.Default).launch {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                // make bitmap square
                var startX = 0
                var startY = 0
                var length = bitmap.width
                if(bitmap.width > bitmap.height) {
                    length = bitmap.height
                    startX = (bitmap.width - bitmap.height) / 2
                    startY = 0
                } else if(bitmap.width < bitmap.height) {
                    length = bitmap.width
                    startX = 0
                    startY = (bitmap.height - bitmap.width) / 2
                }
                val cropped = Bitmap.createBitmap(bitmap, startX, startY, length, length)
                val scaled = Bitmap.createScaledBitmap(cropped, 100, 100, false)

                withContext(Dispatchers.Main) {
                    profilePics.put(uid, scaled)
                    //update userPhoto
                    imageView.setImageBitmap(scaled)
                }
            }
            println("DEBUG: photo successfully loaded")
        }.addOnFailureListener {
            println("DEBUG: photo was not able to load")
        }
    }

    // write a friend request
    // is used with a runBlocking block to make execution wait until writing to the database is finished
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun writeFriendRequest(send: FriendRequest) = suspendCancellableCoroutine<Unit> {
        val toSender: SingularWrite = SingularWrite(
            send,
            getSentRequestsNode(send.sender).child(send.id),
            {
                Log.i("Friends", "added request to sender")
            },
            {
                Log.i("Friends", "failed to add request to sender")
            }
        )
        val toRecipient: SingularWrite = SingularWrite (
            send,
            getFriendRequestsNode(send.recipient).child(send.id),
            {
                Log.i("Friends", "added request to recipient")
            },
            {
                Log.i("Friends", "failed to add request to recipient")
            }
        )
        val writes: ArrayList<SingularWrite> = arrayListOf(toSender, toRecipient)
        // write to database and run callback when done
        val combinedWrite: CombinedWrite = CombinedWrite(writes) { failed ->
            // basically a return statement
            it.resume(Unit) { error ->
                Log.i("Friends", error.toString())
            }
        }
    }

    // returns true if a friend request isn't redundant (not already a friend, not having a pending
    // request to recipient, not having a pending request from recipient to user)
    private suspend fun allowFriendRequest(attemptToSend: FriendRequest): Boolean {
        return runBlocking { getIfRequestShouldBeSent(attemptToSend) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getIfRequestShouldBeSent(request: FriendRequest) =
        suspendCancellableCoroutine<Boolean> {
            var isAlreadyFriend: Boolean = false;
            var isAlreadySent: Boolean = false;
            var isAlreadyBeingAsked: Boolean = false;

            // check if already friend
            val queryFriends: SingularQuery = SingularQuery(
                getFriendsNode(request.sender),
                { friends ->
                    isAlreadyFriend = friends != null && friends.hasChild(request.recipient)
                },
                {
                    Log.i("allowFriendRequest()", it.message.toString())
                }
            )
            // check if user has already sent a request
            val querySentRequests: SingularQuery = SingularQuery(
                getSentRequestsNode(request.sender),
                { sentRequestsSnapshot ->
                    if(sentRequestsSnapshot != null) {
                        sentRequestsSnapshot.children.forEach { sentRequestSnapshot ->
                            val requestTemp: FriendRequest? = sentRequestSnapshot.getValue(FriendRequest::class.java)
                            if(requestTemp != null && request.recipient == requestTemp.recipient) {
                                isAlreadySent = true;
                            }
                        }
                    }
                },
                {
                    Log.i("allowFriendRequest()", it.message.toString())
                }
            )
            // check if user is already being asked by the recipient
            val queryReceivedRequests: SingularQuery = SingularQuery(
                getFriendRequestsNode(request.sender),
                { receivedRequestsSnapshot ->
                    if (receivedRequestsSnapshot != null) {
                        receivedRequestsSnapshot.children.forEach { receivedRequestSnapshot ->
                            val requestTemp: FriendRequest? = receivedRequestSnapshot.getValue(FriendRequest::class.java)
                            if(requestTemp != null && requestTemp.recipient == request.sender) {
                                isAlreadyBeingAsked = true;
                            }
                        }
                    }
                },
                {
                    Log.i("allowFriendRequest()", it.message.toString())
                }
            )

            // start query
            val queries: CombinedQuery = CombinedQuery(arrayListOf(queryFriends, queryReceivedRequests, querySentRequests)) { failed ->
                // basically a return statement
                it.resume(!(isAlreadyFriend || isAlreadySent || isAlreadyBeingAsked)) { error ->
                    Log.i("allowFriendRequest()", error.toString())
                }
            }
        }

    // get profile information on users in list and run a callback when all information is obtained
    private fun getUserInfo(
        users: ArrayList<String>,
        onComplete: (ArrayList<UserProfile>, Boolean) -> Unit
    ) {
        val userInfo: ArrayList<UserProfile> = ArrayList(users.size)

        // init logic of queries that need to be done
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

    // update ui to reflect friend requests in database
    private inner class FriendsRequestPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.i("FriendsRequestPostListener", "friend request onDataChange")
            // put friend requests in list
            val requests: ArrayList<FriendRequest> = ArrayList()
            snapshot.children.forEach() {
                val request: FriendRequest? = it.getValue(FriendRequest::class.java)
                if(request != null) {
                    requests.add(request)
                } else {
                    Log.i("FriendsRequestPostListener", "uid of friend request null")
                }
            }

            // get info on senders of request
            Log.i("FriendsRequestPostListener", "${requests.size} requests")
            val profilesToGet: ArrayList<String> = ArrayList()
            for(request: FriendRequest in requests) {
                profilesToGet.add(request.sender)
            }
            getUserInfo(profilesToGet) { profiles, failed ->
                if(!failed) {
                    val requestEntries: ArrayList<FriendRequestEntry> = ArrayList(profiles.size)
                    for(i in 0 until profiles.size) {
                        requestEntries.add(FriendRequestEntry(profiles[i].userName, requests[i]))
                    }
                    friendsRequests.value = requestEntries
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.i("FriendsActivity", "error with friend request data")
            Log.i("FriendsActivity", error.message)
        }
    }

    // update ui to reflect sent friend requests in database
    private inner class SentRequestPostListener : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            Log.i("FriendsActivity", "sent request onDataChange")
            // put sent requests in a list
            val requests: ArrayList<FriendRequest> = ArrayList()
            snapshot.children.forEach() {
                val request: FriendRequest? = it.getValue(FriendRequest::class.java)
                if(request != null) {
                    requests.add(request)
                } else {
                    Log.i("FriendsActivity", "uid of sent request null")
                }
            }

            // get info on users being asked
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

    // update ui to reflect friends database
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

            // get profiles of friends
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
    }
}

class FriendsActivityViewModelFactory (private val user: FirebaseUser) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(FriendsActivityViewModel::class.java))
            return FriendsActivityViewModel(user) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}