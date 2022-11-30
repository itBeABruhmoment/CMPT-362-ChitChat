package com.example.cmpt_362_chitchat.ui.chatRoom

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.data.Message
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.MalformedURLException
import java.net.URL

class ChatRoomActivity: AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageListener: ValueEventListener

    private lateinit var viewModelFactory: ChatRoomViewModelFactory
    private lateinit var chatRoomViewModel: ChatRoomViewModel

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

        username = sharedPreferences.getString("username", "").toString()
        sendUID = Firebase.auth.currentUser?.uid.toString()
        chatRoom = intent.getStringExtra("chatRoomId").toString()
        chatRoomType= intent.getStringExtra("chatRoomType").toString()
        viewModelFactory = ChatRoomViewModelFactory(chatRoom, chatRoomType)
        chatRoomViewModel =
            ViewModelProvider(this, viewModelFactory)[ChatRoomViewModel::class.java]

        recyclerView = findViewById(R.id.recycler_view)
        messageBox = findViewById(R.id.message)
        sendButton = findViewById(R.id.send_button)
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

        // Initialize video call servcer URL
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

    private fun getUserName() {
        database
            .child("Users")
            .child(sendUID)
            .child("username")
            .get()
            .addOnSuccessListener {
                username = it.value.toString()
            }
            .addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
            }
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_chat_room -> {
                deleteChatRoom()
                return true
            }
            R.id.video_call -> {
                videoCall()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun videoCall() {
        val options:JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
            .setRoom(chatRoom)
            .setWelcomePageEnabled(false)
            .build()
        JitsiMeetActivity.launch(this, options)
    }

}