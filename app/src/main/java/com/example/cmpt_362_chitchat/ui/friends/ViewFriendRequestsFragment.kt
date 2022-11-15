package com.example.cmpt_362_chitchat.ui.friends

import android.app.DownloadManager.Request
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.cmpt_362_chitchat.R

class ViewFriendRequestsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_view_friend_requests, container, false)

        val requests: ArrayList<FriendRequest> = arrayListOf(FriendRequest("A"), FriendRequest("B"), FriendRequest("C"))
        val listView: ListView = view.findViewById(R.id.fragment_view_friend_requests_list)
        val adapter: FriendRequestArrayAdapter = FriendRequestArrayAdapter(requests, requireContext())
        listView.adapter = adapter

        return view
    }
}