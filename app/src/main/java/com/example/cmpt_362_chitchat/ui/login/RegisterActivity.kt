package com.example.cmpt_362_chitchat.ui.login

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cmpt_362_chitchat.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var accountManager: AccountManager
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var name: EditText
    private lateinit var gender: RadioGroup
    private lateinit var selectedGender: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        val email = binding.registerEmail
        val password = binding.registerPassword
        val username = binding.registerUsername
        val register = binding.registerBtn
        //val firstname = binding.registerfirstname
        name = binding.registerlastname
        gender = binding.radioGroupGender

        register.setOnClickListener {
            selectedGender = findViewById(gender.checkedRadioButtonId)
            if (username.text.toString() != "") {
                addAccount(this, email.text.toString(), password.text.toString(), username.text.toString(), name.text.toString(), selectedGender.text.toString())
            } else {
                Toast.makeText(baseContext, "Username is required.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //Adding account to database, initial information includes Email, Password, Username, Name and Gender
    fun addAccount(context: Context, email: String, password: String, username: String, name: String, gender: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            if (it.isSuccessful){
                println("DEBUG REGISTER SUCCESS: email: $email, password: $password")
                auth.currentUser?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(username).build())
                addAccountToDatabase(auth.currentUser?.uid, email, username, name, gender)
                auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                    Toast.makeText(this@RegisterActivity, "Verification email sent to ...", Toast.LENGTH_SHORT).show()
                }?.addOnFailureListener{
                    Toast.makeText(this@RegisterActivity, "Failed to send email verification to ..", Toast.LENGTH_SHORT).show()
                }
                auth.signOut()
                finish()
            } else {
                println("REGISTER FAIL")
            }
        }
    }


    private fun addAccountToDatabase(userId: String?, email: String, username: String, name: String, gender: String) {
        if (userId != null) {
            database
                .child("Users")
                .child(userId)
                .child("username")
                .setValue(username)
            database
                .child("Users")
                .child(userId)
                .child("email")
                .setValue(email)
            database
                .child("Users")
                .child(userId)
                .child("name")
                .setValue(name)
            database
                .child("Users")
                .child(userId)
                .child("gender")
                .setValue(gender)
        } else {
            println("Debug: user not added to db correctly")
        }
    }
}

/*
private fun getAccount(context: Context): Account? {
    accountManager = AccountManager.get(context)
    var acc: Account? = null
    try {
        acc = accountManager.getAccountsByType("com.login.example")[0]
        println("DEBUG: USERNAME: ${acc.name}")

    }catch (E: Throwable){

    }
    return acc
}*/
