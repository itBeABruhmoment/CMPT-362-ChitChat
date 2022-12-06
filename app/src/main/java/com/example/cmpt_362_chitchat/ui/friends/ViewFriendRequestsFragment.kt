package com.example.cmpt_362_chitchat.ui.friends

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ViewFriendRequestsFragment : Fragment() {
    private lateinit var viewModel: FriendsActivityViewModel
    private lateinit var friendRequestsListView: ListView
    private lateinit var sentRequestsListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_view_friend_requests, container, false)

        val userEmail = view.findViewById<EditText>(R.id.search_user)

        // get viewmodel
        val currentUser: FirebaseUser? = Firebase.auth.currentUser
        if (currentUser == null) {
            Log.i("FriendsActivity", "user null, going to login page")
            val intent: Intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        } else {
            Log.i("FriendsActivity", "user not null, continue")
            val viewModelFactory: FriendsActivityViewModelFactory = FriendsActivityViewModelFactory(currentUser)
            viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(FriendsActivityViewModel::class.java)

            // logic of friend adding via email
            view.findViewById<Button>(R.id.fragment_view_friend_requests_test).setOnClickListener {
                val currentUserEmail = currentUser.email
                val user = userEmail.text.toString()
                if (user == "") {
                    Toast.makeText(requireContext(), "User email required", Toast.LENGTH_SHORT).show()
                } else {
                    var recipientId = ""
                    FirebaseDatabase.getInstance().reference
                        .child("Users")
                        .get().addOnSuccessListener {
                            for (snapshot in it.children) {
                                val email: String? = snapshot.child("email").getValue(String::class.java)
                                Log.d("email", email.toString())
                                if (email != null && email == user /* && email != currentUserEmail */) {
                                    recipientId = snapshot.key.toString()
                                    viewModel.addFriendRequest(currentUser.uid, recipientId)
                                    userEmail.text.clear()
                                }
                            }
                        }
                }
            }
        }

        // init list view for viewing received friend requests
        friendRequestsListView = view.findViewById(R.id.fragment_view_friend_requests_list)
        viewModel.friendsRequests.observe(requireActivity()) { requests ->
            Log.i("FriendsActivity", "friend request live data update ${requests.size}")
            val adapter: FriendRequestArrayAdapter = FriendRequestArrayAdapter(
                requests,
                requireActivity(),
                viewModel
            )
            friendRequestsListView.adapter = adapter
        }

        // init list view for viewing sent friend requests
        sentRequestsListView = view.findViewById(R.id.fragment_view_sent_requests_list)
        viewModel.sentRequests.observe(requireActivity()) { sent ->
            Log.i("FriendsActivity", "sent request live data update ${sent.size}")
            val adapter: SentRequestArrayAdapter = SentRequestArrayAdapter(
                sent,
                requireActivity(),
                viewModel
            )
            sentRequestsListView.adapter = adapter
        }

        return view
    }
}