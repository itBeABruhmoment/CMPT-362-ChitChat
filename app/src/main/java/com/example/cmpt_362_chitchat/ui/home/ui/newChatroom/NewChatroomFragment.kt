package com.example.cmpt_362_chitchat.ui.home.ui.newChatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.databinding.FragmentNewChatroomBinding

class NewChatroomFragment : Fragment() {
    companion object {
        val chatroomTypes = arrayOf("Private", "Public")
    }

    private var _binding: FragmentNewChatroomBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val newChatroomViewModel =
            ViewModelProvider(this).get(NewChatroomViewModel::class.java)

        _binding = FragmentNewChatroomBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val chatroomTypeSpinner: Spinner = binding.spinnerChatroomType
        val chatroomTypeAdapter: ArrayAdapter<String> =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, chatroomTypes)
        chatroomTypeSpinner.adapter = chatroomTypeAdapter

        val newChatroomButton: Button = binding.buttonNewChatroom
        newChatroomButton.setOnClickListener {
            // TODO: open new chatroom activity
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}