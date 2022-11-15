package com.example.cmpt_362_chitchat.ui.profile


import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.cmpt_362_chitchat.R

class Dialog : DialogFragment(), DialogInterface.OnClickListener {
    companion object {
        const val DIALOG_KEY = "DIALOG"
        const val PROFILE_STRING_DIALOG = 1
        const val GENDER_DIALOG = 2
        const val PASSWORD_DIALOG = 3
        const val USERNAME_DIALOG = 4
        const val NAME_DIALOG = 5
        const val PHOTO_DIALOG = 6
    }

    private lateinit var profileEditText : EditText
    private lateinit var title: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        lateinit var dialog: Dialog
        val dialogID = arguments?.getInt(DIALOG_KEY)
        val builder = AlertDialog.Builder(requireActivity())

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
            }

            USERNAME_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_string,
                    null
                )
                profileEditText = view.findViewById(R.id.Edit)
                title = view.findViewById(R.id.profileTitle)
                builder.setView(view)
                title.text = "New Username"
                builder.setPositiveButton("SAVE", this)
            }

            NAME_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_string,
                    null
                )
                profileEditText = view.findViewById(R.id.Edit)
                title = view.findViewById(R.id.profileTitle)
                builder.setView(view)
                title.text = "New name"
                builder.setPositiveButton("SAVE", this)
            }

            GENDER_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_gender,
                    null
                )
                builder.setView(view)
                builder.setPositiveButton("SAVE", this)
            }

            PASSWORD_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_password,
                    null
                )
                builder.setView(view)
                builder.setPositiveButton("SAVE", this)
            }

            PHOTO_DIALOG -> {
                val view = requireActivity().layoutInflater.inflate(
                    R.layout.fragment_dialog_profile_change_photo,
                    null
                )
                builder.setView(view)
                builder.setPositiveButton("SAVE", this)
            }
        }
        builder.setNegativeButton("CANCEL", this)
        dialog = builder.create()
        return dialog
    }

    //not done
    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            println("DEBUG: CLICKED POSITIVE")
        } else if (which ==DialogInterface.BUTTON_NEGATIVE) {
            println("DEBUG: NEGATIVE")
        }
    }


}