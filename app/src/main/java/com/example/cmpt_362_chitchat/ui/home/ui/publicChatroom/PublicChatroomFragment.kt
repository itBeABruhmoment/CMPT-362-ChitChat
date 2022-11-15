package com.example.cmpt_362_chitchat.ui.home.ui.publicChatroom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.databinding.FragmentPublicChatroomBinding
import com.example.cmpt_362_chitchat.ui.chatRoom.ChatRoomActivity
import com.example.cmpt_362_chitchat.ui.home.adapters.ChatroomListAdapter

class PublicChatroomFragment : Fragment() {
    // TODO: replace later
    private val tempChatrooms = arrayOf(
        arrayOf("Public Chatroom 1", "Text preview"),
        arrayOf("Public Chatroom 2", "Text preview"))

    private var _binding: FragmentPublicChatroomBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val publicChatroomViewModel =
            ViewModelProvider(this).get(PublicChatroomViewModel::class.java)

        _binding = FragmentPublicChatroomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val chatroomsList = binding.listPublicChatroom
        val chatroomsAdapter = ChatroomListAdapter(
            this.requireContext(),
            android.R.layout.simple_list_item_2,
            tempChatrooms
        )
        chatroomsList.adapter = chatroomsAdapter
        chatroomsList.setOnItemClickListener { parent, view, position, id ->
            startActivity(Intent(requireActivity(), ChatRoomActivity::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}