package com.example.cmpt_362_chitchat.ui.home

import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE)
        getUsername()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_private_chatrooms,
                R.id.navigation_public_chatrooms,
                R.id.navigation_new_chatroom,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.navigate(intent.getIntExtra(START_NAVIGATED_TO_INTENT, R.id.navigation_private_chatrooms))
    }

    private fun getUsername() {
        database
            .child("Users")
            .child(auth.currentUser?.uid.toString())
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").value.toString()
                    val editPrefs = sharedPreferences.edit()
                    editPrefs?.putString("username", username)
                    editPrefs?.apply()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    companion object {
        val START_NAVIGATED_TO_INTENT: String = "start_navigated_to"
    }
}