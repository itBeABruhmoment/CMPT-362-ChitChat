package com.example.cmpt_362_chitchat.ui.home.ui.newChatRoom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
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

        val chatroomTypeSpinner: Spinner = binding.spinnerChatroomType
        val chatroomTypeAdapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, chatroomTypes)
        chatroomTypeSpinner.adapter = chatroomTypeAdapter

        val chatRoomNameEditText: EditText = binding.chatRoomName

        val newChatroomButton: Button = binding.buttonNewChatroom
        newChatroomButton.setOnClickListener {
            val newChatRoomId = UUID.randomUUID().toString()
            val chatRoomType = chatroomTypeSpinner.selectedItem.toString()
            val chatRoomName = chatRoomNameEditText.text

            database
                .child("ChatRooms")
                .child(chatRoomType)
                .child(newChatRoomId)
                .child("ChatRoomName")
                .child("ownerId")
                .setValue(auth.currentUser?.uid)

            database
                .child("ChatRooms")
                .child(chatRoomType)
                .child(newChatRoomId)
                .child("ChatRoomName")
                .setValue(chatRoomName)

            if (chatRoomType == "Private") {
                database.child("Users")
                    .child(auth.currentUser?.uid.toString())
                    .child("ChatRooms")
                    .child(newChatRoomId)
                    .setValue(true)
            }

            val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
            intent.putExtra("chatRoomId", newChatRoomId)
            intent.putExtra("chatRoomType", chatRoomType)
            intent.putExtra("chatRoomName", chatRoomName)
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}