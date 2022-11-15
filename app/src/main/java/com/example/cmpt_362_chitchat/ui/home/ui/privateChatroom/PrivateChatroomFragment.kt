package com.example.cmpt_362_chitchat.ui.home.ui.privateChatroom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.databinding.FragmentPrivateChatroomBinding
import com.example.cmpt_362_chitchat.ui.chatRoom.ChatRoomActivity
import com.example.cmpt_362_chitchat.ui.home.adapters.ChatroomListAdapter

class PrivateChatroomFragment : Fragment() {

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
            privateChatroomViewModel.chatrooms.value!!
        )

        privateChatroomViewModel.chatrooms.observe(viewLifecycleOwner) { it ->
            chatroomsAdapter.replace(it)
            chatroomsAdapter.notifyDataSetChanged()
        }

        chatroomsList.adapter = chatroomsAdapter
        chatroomsList.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
            intent.putExtra("chatroomId", privateChatroomViewModel.getChatroom(position))
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}