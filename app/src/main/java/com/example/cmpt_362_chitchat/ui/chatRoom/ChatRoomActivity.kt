package com.example.cmpt_362_chitchat.ui.chatRoom

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.data.Message
import com.google.firebase.database.*

class ChatRoomActivity: AppCompatActivity() {

    var chatRoom: String = "123456"
    private val sendUID: String = "sample 2"

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        supportActionBar?.title = "Sample chat room"

        val chatroomId = intent.getStringExtra("chatroomId")
        if (chatroomId != null) {
            chatRoom = chatroomId
        }

        recyclerView = findViewById(R.id.recycler_view)
        messageBox = findViewById(R.id.message)
        sendButton = findViewById(R.id.send_button)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, sendUID)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        database = FirebaseDatabase.getInstance().reference

        database.child("ChatRooms").child(chatRoom).child("messages")
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
            val message = Message(messageBox.text.toString(), sendUID)

            database.child("ChatRooms").child(chatRoom).child("messages").push()
                .setValue(message)
            messageBox.onEditorAction(EditorInfo.IME_ACTION_DONE)
            messageBox.setText("")
        }

    }



}