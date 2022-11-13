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
    }

    private lateinit var profileEditText : EditText

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
                builder.setTitle("Change X")
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