package com.example.cmpt_362_chitchat.ui.profile

import android.app.AlertDialog
import androidx.lifecycle.ViewModel

//viewModel for passing dialog info
var dialogID = -1
private lateinit var dialog: AlertDialog

class ProfileViewModel : ViewModel() {

    fun setDialogID(id: Int) {
        dialogID = id
    }

    fun getDialogID(): Int {
        return dialogID
    }

    fun setDialog(d: AlertDialog) {
        dialog = d
    }

    fun getDialog(): AlertDialog {
        return dialog
    }

}