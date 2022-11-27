package com.example.cmpt_362_chitchat.ui.home.ui.publicChatRoom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.databinding.FragmentPublicChatRoomBinding
import com.example.cmpt_362_chitchat.ui.chatRoom.ChatRoomActivity
import com.example.cmpt_362_chitchat.ui.home.adapters.ChatRoomListAdapter

class PublicChatRoomFragment : Fragment() {

    private var _binding: FragmentPublicChatRoomBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val publicChatRoomViewModel =
            ViewModelProvider(this).get(PublicChatRoomViewModel::class.java)

        _binding = FragmentPublicChatRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val chatroomsList = binding.listPublicChatroom
        val chatroomsAdapter = ChatRoomListAdapter(
            this.requireContext(),
            android.R.layout.simple_list_item_2,
            publicChatRoomViewModel.chatrooms.value!!
        )

        publicChatRoomViewModel.chatrooms.observe(viewLifecycleOwner) { it ->
            chatroomsAdapter.replace(it)
            chatroomsAdapter.notifyDataSetChanged()
        }

        chatroomsList.adapter = chatroomsAdapter
        chatroomsList.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
            intent.putExtra("chatRoomId", publicChatRoomViewModel.getChatroom(position))
            intent.putExtra("chatRoomType", "Public")
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}