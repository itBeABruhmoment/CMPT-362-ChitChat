package com.example.cmpt_362_chitchat.ui.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ManageFriendsFragment : Fragment() {
    private lateinit var viewModel: FriendsActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_manage_friends, container, false)

        // get viewmodel
        val tempUser: FirebaseUser? = Firebase.auth.currentUser
        if(tempUser == null) {
            Log.i("FriendsActivity", "user null, going to login page")
            val intent: Intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        } else {
            Log.i("FriendsActivity", "user not null, continue")
            val viewModelFactory: FriendsActivityViewModelFactory = FriendsActivityViewModelFactory(tempUser)
            viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(FriendsActivityViewModel::class.java)

            val listView: ListView = view.findViewById(R.id.fragment_manage_friends_list)
            viewModel.friends.observe(requireActivity()) { friends ->
                val adapter: FriendsArrayAdapter = FriendsArrayAdapter(friends, requireActivity(), viewModel, tempUser)
                listView.adapter = adapter
            }
        }
        return view
    }
}