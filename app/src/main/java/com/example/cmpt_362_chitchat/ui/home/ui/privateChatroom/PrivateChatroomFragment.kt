package com.example.cmpt_362_chitchat.ui.home.ui.privateChatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.databinding.FragmentPrivateChatroomBinding
import com.example.cmpt_362_chitchat.ui.home.adapters.ChatroomListAdapter

class PrivateChatroomFragment : Fragment() {
    // TODO: replace later
    private val tempChatrooms = arrayOf(
        arrayOf("Private Chatroom 1", "Text preview"),
        arrayOf("Private Chatroom 2", "Text preview"))

    private var _binding: FragmentPrivateChatroomBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val privateChatroomViewModel =
            ViewModelProvider(this).get(PrivateChatroomViewModel::class.java)

        _binding = FragmentPrivateChatroomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val chatroomsList = binding.listPrivateChatroom
        val chatroomsAdapter = ChatroomListAdapter(
            this.requireContext(),
            android.R.layout.simple_list_item_2,
            tempChatrooms
        )
        chatroomsList.adapter = chatroomsAdapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}