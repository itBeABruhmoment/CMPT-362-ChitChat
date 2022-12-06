package com.example.cmpt_362_chitchat.ui.chatRoom

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.data.Message
import com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom.CustomEditText
import com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom.CustomEditText.KeyBoardInputCallbackListener
import com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom.FriendsViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.MalformedURLException
import java.net.URL


class ChatRoomActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageBox: CustomEditText
    private lateinit var sendButton: ImageView
    private lateinit var imagePreview: ImageView
    private lateinit var imageUri: Uri
    private lateinit var cancelImage: ImageView
    private var sendImage: Boolean = false

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageListener: ValueEventListener

    private lateinit var viewModelFactory: ChatRoomViewModelFactory
    private lateinit var chatRoomViewModel: ChatRoomViewModel

    // Friend code borrowed from NewChatRoomFragment
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var friendsUserIDs: ArrayList<String>
    private lateinit var friendUsernames: ArrayList<String>
    private lateinit var friendsSelected: BooleanArray
    private lateinit var participantsBuilder: AlertDialog.Builder
    private lateinit var friendsBuilder: AlertDialog.Builder

    private lateinit var sendUID: String
    private lateinit var chatRoom: String
    private lateinit var chatRoomType: String
    private lateinit var chatRoomName: String
    private lateinit var username: String

    private var currentUserIsParticipant = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)

        database = FirebaseDatabase.getInstance().reference

        chatRoomName = intent.getStringExtra("chatRoomName").toString()

        supportActionBar?.title = chatRoomName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        username = sharedPreferences.getString("username", "").toString()
        sendUID = Firebase.auth.currentUser?.uid.toString()
        chatRoom = intent.getStringExtra("chatRoomId").toString()
        chatRoomType= intent.getStringExtra("chatRoomType").toString()
        viewModelFactory = ChatRoomViewModelFactory(chatRoom, chatRoomType)
        chatRoomViewModel = ViewModelProvider(this, viewModelFactory)[ChatRoomViewModel::class.java]

        participantsBuilder = AlertDialog.Builder(this)
        participantsBuilder.setTitle("Participants")
        // Friend View Model to observe friend list (borrowed from NewChatRoomFragment)
        friendsViewModel = ViewModelProvider(this)[FriendsViewModel::class.java]
        friendsBuilder = AlertDialog.Builder(this)
        friendsBuilder.setTitle("Friends")
        friendsViewModel.friendIds.observe(this) { it ->
            friendsUserIDs = it
        }
        friendsViewModel.friendUsernames.observe(this) { it ->
            friendUsernames = it
            updateFriendOptions()
        }

        recyclerView = findViewById(R.id.recycler_view)
        messageBox = findViewById(R.id.message)
        sendButton = findViewById(R.id.send_button)
        imagePreview = findViewById(R.id.image_preview)
        imagePreview.visibility = View.GONE
        cancelImage = findViewById(R.id.cancel_image)
        cancelImage.visibility = View.GONE
        cancelImage.setOnClickListener {
            sendImage = false
            imagePreview.visibility = View.GONE
            cancelImage.visibility = View.GONE
        }

        setUpImageKeyboardSupport()

        setUpMessageAndParticipantMonitoring()

        sendMessageToFirebase()

        setUpVideoCall()
    }

    private fun updateFriendOptions() {
        // Ignore friends already in chat room
        val participantIDs = ArrayList<String>(chatRoomViewModel.participants.value!!.values)
        for (id in participantIDs) {
            friendsUserIDs.remove(id)
            friendUsernames.remove(chatRoomViewModel.participants.value!![id])
        }
        friendsSelected = BooleanArray(friendsViewModel.friendIds.value!!.size) { false }

        friendsBuilder.setMultiChoiceItems(friendUsernames.toTypedArray(), friendsSelected) { _, which, isChecked ->
            friendsSelected[which] = isChecked
        }
    }

    private fun setUpImageKeyboardSupport() {
        messageBox.setKeyBoardInputCallbackListener(object : KeyBoardInputCallbackListener {
            override fun onCommitContent(
                inputContentInfo: InputContentInfoCompat?,
                flags: Int, opts: Bundle?
            ) {
                sendImage = true

                imageUri = inputContentInfo?.contentUri!!
                imagePreview.setImageURI(imageUri)

                imagePreview.visibility = View.VISIBLE
                cancelImage.visibility = View.VISIBLE
            }
        })
    }

    private fun setUpMessageAndParticipantMonitoring() {
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, sendUID, hashMapOf())

        chatRoomViewModel.participants.observe(this) { it ->
            messageAdapter.replace(it)
            messageAdapter.notifyDataSetChanged()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        messageListener = database
            .child("ChatRooms")
            .child(chatRoomType)
            .child(chatRoom)
            .child("messages")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()

                    for(snap in snapshot.children) {
                        val message = snap.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun sendMessageToFirebase() {
        sendButton.setOnClickListener {
            println("Clicked")

            if (!currentUserIsParticipant && !chatRoomViewModel.getParticipantIds()?.contains(sendUID)!!) {
                database
                    .child("ChatRooms")
                    .child(chatRoomType)
                    .child(chatRoom)
                    .child("Participants")
                    .child(sendUID)
                    .setValue(true)
                currentUserIsParticipant = true
            }

            // Send message with image
            if(sendImage) {

                val cut = imageUri.toString().lastIndexOf("/")
                val imageName = imageUri.toString().substring(cut + 1)

                storage = FirebaseStorage.getInstance().reference

                storage
                    .child("SentImages")
                    .child(sendUID)
                    .child(imageName)
                    .putFile(imageUri)

                val message = Message(messageBox.text.toString(), sendUID, imageName)
                database
                    .child("ChatRooms")
                    .child(chatRoomType)
                    .child(chatRoom)
                    .child("messages")
                    .push()
                    .setValue(message)
                messageBox.onEditorAction(EditorInfo.IME_ACTION_DONE)
                messageBox.setText("")

                sendImage = false
                imagePreview.visibility = View.GONE
                cancelImage.visibility = View.GONE

            }
            // Send message without any attached image
            else {
                if (messageBox.text.toString() != "") {

                    val message = Message(messageBox.text.toString(), sendUID)
                    database
                        .child("ChatRooms")
                        .child(chatRoomType)
                        .child(chatRoom)
                        .child("messages")
                        .push()
                        .setValue(message)
                    messageBox.onEditorAction(EditorInfo.IME_ACTION_DONE)
                    messageBox.setText("")
                }
            }
        }
    }

    private fun setUpVideoCall() {
        // Initialize video call server URL
        try {
            val serverURL = URL("https://meet.jit.si")
            val defaultOptions = JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setWelcomePageEnabled(false)
                .build()
            JitsiMeet.setDefaultConferenceOptions(defaultOptions)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    private fun videoCall() {
        val options:JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
            .setRoom(chatRoom)
            .setWelcomePageEnabled(false)
            .build()
        JitsiMeetActivity.launch(this, options)
    }

    override fun onDestroy() {
        super.onDestroy()
        database
            .child("ChatRooms")
            .child(chatRoomType)
            .child(chatRoom)
            .child("messages").removeEventListener(messageListener)
        database
            .child("ChatRooms")
            .child(chatRoomType)
            .child(chatRoom)
            .child("Participants").removeEventListener(chatRoomViewModel.participantsListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_chat_room, menu)
        // Disable participants view in public chat rooms
        if(intent.getStringExtra("chatRoomType").toString() == "Public") {
            menu!!.getItem(0).isVisible = false
            menu.getItem(1).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_chat_room -> {
                deleteChatRoom()
                return true
            }
            R.id.add_friends -> {
                showAddFriendsDialog()
                return true
            }
            R.id.video_call -> {
                videoCall()
                return true
            }
            R.id.participants -> {
                showParticipants()
                return true
            }
            android.R.id.home -> {
                this.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAddFriendsDialog() {
        friendsBuilder.setPositiveButton("Ok") { _, _ -> addFriends() }
        friendsBuilder.setNegativeButton("Cancel", null)

        val alertDialog: AlertDialog = friendsBuilder.create()
        alertDialog.show()

        val newParticipantIDs = ArrayList<String>()
        for ((index, isFriendSelected) in friendsSelected.withIndex()) {
            if (isFriendSelected) {
                newParticipantIDs.add(friendsUserIDs[index])
            }
        }
    }

    private fun addFriends() {

        val newParticipantIDs = ArrayList<String>()
        for ((index, isFriendSelected) in friendsSelected.withIndex()) {
            if (isFriendSelected) {
                newParticipantIDs.add(friendsUserIDs[index])
            }
        }

        database
            .child("ChatRooms")
            .child(chatRoomType)
            .child(chatRoom)
            .get().addOnCompleteListener {
                    val ownerId = it.result.child("ownerId").value
                    if (sendUID == ownerId) {
                        println("HERE")
                        for (participant in newParticipantIDs) {
                            println(participant)
                            database.child("Users")
                                .child(participant)
                                .child("ChatRooms")
                                .child(chatRoom)
                                .setValue(true)
                        }
                    } else {
                        Toast.makeText(
                            baseContext, "You don't have permission.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

    }

    private fun showParticipants() {

        val participantUsernames = ArrayList<String>(chatRoomViewModel.participants.value!!.values)

        val listView = ListView(this)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            participantUsernames
        )
        listView.adapter = adapter

        participantsBuilder.setView(listView)
        participantsBuilder.setPositiveButton("Ok", null)
        participantsBuilder.setNegativeButton("Cancel", null)

        val alertDialog: AlertDialog = participantsBuilder.create()
        alertDialog.show()
    }

    private fun deleteChatRoom() {
        database
            .child("ChatRooms")
            .child(chatRoomType)
            .child(chatRoom)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ownerId = snapshot.child("ownerId").value
                    if (sendUID == ownerId) {
                        snapshot.ref.removeValue()

                        if (chatRoomType == "Private") {
                            database
                                .child("Users")
                                .child(sendUID)
                                .child("ChatRooms")
                                .child(chatRoom)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        snapshot.ref.removeValue()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }
                                })
                        }

                        finish()
                    } else {
                        Toast.makeText(baseContext, "You don't have permission.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

}