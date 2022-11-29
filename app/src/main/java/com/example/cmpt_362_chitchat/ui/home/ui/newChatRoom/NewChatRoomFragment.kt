package com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
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
        // TODO: get friends from db
        val friendIds = arrayOf("")
        val friendsSelected = BooleanArray(friendIds.size) { false }
    }

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentNewChatRoomBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        _binding = FragmentNewChatRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val addFriends = binding.addFriendsChatRoom
        addFriends.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Add Friends")

            builder.setMultiChoiceItems(friendIds, friendsSelected) { _, which, isChecked ->
                friendsSelected[which] = isChecked
            }

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
                    addFriends.visibility = View.INVISIBLE
                }
            }
        }
        chatroomTypeSpinner.adapter = chatroomTypeAdapter

        val newChatroomButton: Button = binding.buttonNewChatroom
        newChatroomButton.setOnClickListener {
            val newChatRoomId = UUID.randomUUID().toString()
            val chatRoomType = chatroomTypeSpinner.selectedItem.toString()

            database
                .child("ChatRooms")
                .child(chatRoomType)
                .child(newChatRoomId)
                .child("ownerId")
                .setValue(auth.currentUser?.uid)

            if (chatRoomType == "Private") {
                val participants = friendIds.toCollection(ArrayList())
                participants.add(auth.currentUser?.uid.toString())

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
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}