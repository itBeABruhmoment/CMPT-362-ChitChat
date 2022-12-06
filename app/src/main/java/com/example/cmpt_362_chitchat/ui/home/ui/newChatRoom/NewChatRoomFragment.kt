package com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.databinding.FragmentNewChatRoomBinding
import com.example.cmpt_362_chitchat.ui.chatRoom.ChatRoomActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.*

class NewChatRoomFragment : Fragment() {
    companion object {
        val chatroomTypes = arrayOf("Private", "Public")
    }

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    private var _binding: FragmentNewChatRoomBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var friendIds: Array<String>
    private lateinit var friendUsernames: Array<String>
    private lateinit var friendsSelected: BooleanArray

    private lateinit var userId: String
    private lateinit var username: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid.toString()
        sharedPreferences = requireContext().getSharedPreferences("sharedPreferences", AppCompatActivity.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "").toString()

        val newChatRoomViewModel =
            ViewModelProvider(this).get(FriendsViewModel::class.java)

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Friends")

        newChatRoomViewModel.friendIds.observe(viewLifecycleOwner) { it ->
            friendIds = it.toTypedArray()
            friendsSelected = BooleanArray(friendIds.size) { false }
        }

        newChatRoomViewModel.friendUsernames.observe(viewLifecycleOwner) { it ->
            friendUsernames = it.toTypedArray()
            friendsSelected = BooleanArray(friendIds.size) { false }
            updateFriendOptions(builder)
        }

        _binding = FragmentNewChatRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val addFriends = binding.addFriendsChatRoom
        addFriends.setOnClickListener {
            updateFriendOptions(builder)

            builder.setPositiveButton("Ok", null)
            builder.setNegativeButton("Cancel", null)

            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }

        val chatroomTypeSpinner: Spinner = binding.spinnerChatroomType
        val chatroomTypeAdapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), R.xml.spinner_item, chatroomTypes)
        chatroomTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    addFriends.visibility = View.VISIBLE
                } else {
                    friendsSelected = BooleanArray(friendIds.size) { false }
                    addFriends.visibility = View.INVISIBLE
                }
            }
        }
        chatroomTypeSpinner.adapter = chatroomTypeAdapter

        val chatRoomNameEditText: EditText = binding.chatRoomName

        val newChatroomButton: Button = binding.buttonNewChatroom
        newChatroomButton.setOnClickListener {
            val newChatRoomId = UUID.randomUUID().toString()
            val chatRoomType = chatroomTypeSpinner.selectedItem.toString()
            var chatRoomName = chatRoomNameEditText.text.toString()

            val participants = ArrayList<String>()
            participants.add(userId)
            for ((index, isFriendSelected) in friendsSelected.withIndex()) {
                if (isFriendSelected) {
                    participants.add(friendIds[index])
                }
            }

            if (chatRoomName == "") {
                chatRoomName = username
                for (participant in participants) {
                    if (participant != userId) {
                        println("Debug: test ${participants.indexOf(participant)}")
                        chatRoomName =
                            "$chatRoomName, ${friendUsernames[participants.indexOf(participant) - 1]}"
                    }
                }
            }

            database
                .child("ChatRooms")
                .child(chatRoomType)
                .child(newChatRoomId)
                .child("ownerId")
                .setValue(auth.currentUser?.uid)

            database
                .child("ChatRooms")
                .child(chatRoomType)
                .child(newChatRoomId)
                .child("ChatRoomName")
                .setValue(chatRoomName)

            if (chatRoomType == "Private") {

                for (participant in participants) {
                    database.child("Users")
                        .child(participant)
                        .child("ChatRooms")
                        .child(newChatRoomId)
                        .setValue(true)
                }
            }

            val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
            intent.putExtra("chatRoomId", newChatRoomId)
            intent.putExtra("chatRoomType", chatRoomType)
            intent.putExtra("chatRoomName", chatRoomName)
            intent.putExtra("friendList", friendIds)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateFriendOptions(builder: AlertDialog.Builder) {
        builder.setMultiChoiceItems(friendUsernames, friendsSelected) { _, which, isChecked ->
            friendsSelected[which] = isChecked
        }
    }
}