package com.example.cmpt_362_chitchat.ui.profile

import android.app.AlertDialog
import androidx.lifecycle.ViewModel

//viewModel for passing dialog info
var dialogID = -1
var gender = ""
private lateinit var dialog: AlertDialog

class ProfileViewModel : ViewModel() {
    fun setGender(g: String) {
        gender = g
    }

    fun returnGender(): String {
        return gender
    }

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