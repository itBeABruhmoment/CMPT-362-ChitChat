package com.example.cmpt_362_chitchat.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.cmpt_362_chitchat.R

class ManageFriendsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_manage_friends, container, false)

        val friends: ArrayList<User> = arrayListOf(User("Dominic"), User("Leonel"), User("Coleman"), User("Mitchell"))
        val listView: ListView = view.findViewById(R.id.fragment_manage_friends_list)
        val adapter: FriendsArrayAdapter = FriendsArrayAdapter(friends, requireActivity())
        listView.adapter = adapter

        return view
    }
}