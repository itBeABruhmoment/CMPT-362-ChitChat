package com.example.cmpt_362_chitchat.ui.profile

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.cmpt_362_chitchat.R

class ProfileActivity : AppCompatActivity() {
    private lateinit var profileItems: ListView

    private val profileDescription = arrayOf(
        "Username", "Name", "DOB", "Gender", "Password"
    )

    //replace with database later
    private val placeHolderDatabaseInfo = arrayOf(
        "usernamePlaceHolder", "namePlacerHolder", "DOBPlacerHolder", "genderPlaceHolder", "passwordPlaceHolder"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profileItems = findViewById(R.id.profileItems)

        val profileAdapter = ProfileAdapter(this, profileDescription, placeHolderDatabaseInfo)
        profileItems?.adapter = profileAdapter

        //data not saved atm
        profileItems?.setOnItemClickListener(){adapterView, view, position, id ->
            val itemAtPos = adapterView.getItemAtPosition(position)
            println("DEBUG: $itemAtPos")

            //dialogs all the same for now
            val newDialog  = Dialog()
            val bundle = Bundle()
            bundle.putInt(Dialog.DIALOG_KEY, Dialog.PROFILE_STRING_DIALOG)
            newDialog.arguments = bundle
            newDialog.show(supportFragmentManager, "standard string")
        }
    }
}