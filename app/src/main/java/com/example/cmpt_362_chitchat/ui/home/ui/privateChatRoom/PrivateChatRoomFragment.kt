package com.example.cmpt_362_chitchat.ui.home.ui.privateChatRoom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.databinding.FragmentPrivateChatRoomBinding
import com.example.cmpt_362_chitchat.ui.chatRoom.ChatRoomActivity
import com.example.cmpt_362_chitchat.ui.home.adapters.ChatRoomListAdapter

class PrivateChatRoomFragment : Fragment() {

    private var _binding: FragmentPrivateChatRoomBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val privateChatRoomViewModel =
            ViewModelProvider(this).get(PrivateChatRoomViewModel::class.java)

        _binding = FragmentPrivateChatRoomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val chatroomsList = binding.listPrivateChatroom
        val chatroomsAdapter = ChatRoomListAdapter(
            this.requireContext(),
            android.R.layout.simple_list_item_2,
            privateChatRoomViewModel.chatRoomNames.value!!
        )

        privateChatRoomViewModel.chatRoomNames.observe(viewLifecycleOwner) {
            chatroomsAdapter.replace(it)
            chatroomsAdapter.notifyDataSetChanged()
        }

        chatroomsList.adapter = chatroomsAdapter
        chatroomsList.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(requireActivity(), ChatRoomActivity::class.java)
            intent.putExtra("chatRoomId", privateChatRoomViewModel.getChatroom(position))
            intent.putExtra("chatRoomType", "Private")
            intent.putExtra("chatRoomName", privateChatRoomViewModel.chatRoomNames.value!![position])
            startActivity(intent)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}