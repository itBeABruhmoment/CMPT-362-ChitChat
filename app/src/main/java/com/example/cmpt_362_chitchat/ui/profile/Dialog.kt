package com.example.cmpt_362_chitchat.ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase




class Dialog : DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        const val DIALOG_KEY = "DIALOG"
        const val PROFILE_STRING_DIALOG = 1
        const val GENDER_DIALOG = 2
        const val PASSWORD_DIALOG = 3
        const val USERNAME_DIALOG = 4
        const val NAME_DIALOG = 5
        const val PHOTO_DIALOG = 6
        const val EMAIL_DIALOG = 7
        const val NAME_LAST_DIALOG = 8
    }

    private val genderOptions = arrayOf(
        "Female", "Male", "Other"
    )

    private lateinit var profileEditText : EditText


    private lateinit var viewModel: ProfileViewModel

    private lateinit var emailEditText : EditText
    private lateinit var user: FirebaseAuth
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        lateinit var dialog: Dialog
        val dialogID = arguments?.getInt(DIALOG_KEY)
        val builder = AlertDialog.Builder(requireActivity())
        //access to view Model
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        user = Firebase.auth

        //create dialogs
        when (dialogID) {
            PROFILE_STRING_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_string,
                    null
                )
                profileEditText = view.findViewById(R.id.Edit)
                builder.setView(view)
                builder.setTitle("Change Username")
                builder.setPositiveButton("SAVE", this)
                builder.setNegativeButton("CANCEL", this)
            }

            EMAIL_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_string,
                    null
                )
                emailEditText = view.findViewById(R.id.Edit)
                builder.setView(view)
                emailEditText.hint = "New email"
                viewModel.setDialogID(dialogID)
            }

            USERNAME_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_string,
                    null
                )
                profileEditText = view.findViewById(R.id.Edit)
                builder.setView(view)
                profileEditText.hint = "New username"
                viewModel.setDialogID(dialogID)
            }

            NAME_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_string,
                    null
                )
                profileEditText = view.findViewById(R.id.Edit)
                builder.setView(view)
                profileEditText.hint = "New name"
                viewModel.setDialogID(dialogID)
            }

            NAME_LAST_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_string,
                    null
                )
                profileEditText = view.findViewById(R.id.Edit)
                builder.setView(view)
                profileEditText.hint = "Last name"
                viewModel.setDialogID(dialogID)
            }

            GENDER_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_gender,
                    null
                )
                viewModel.setGender("") //clean viewModel
                builder.setView(view)
                builder.setTitle("Please select your gender")
                builder.setSingleChoiceItems(genderOptions, -1,
                    DialogInterface.OnClickListener { dialog, item ->
                        viewModel.setGender(genderOptions.get(item))
                    })
                viewModel.setDialogID(dialogID)
            }

            PASSWORD_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_password,
                    null
                )
                builder.setView(view)
                viewModel.setDialogID(dialogID)
            }

            PHOTO_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_change_photo,
                    null
                )
                builder.setView(view)
            }
        }

        dialog = builder.create()
        viewModel.setDialog(dialog)
        return dialog
    }

    //not used
    override fun onClick(dialog: DialogInterface?, which: Int) {
    }






}

