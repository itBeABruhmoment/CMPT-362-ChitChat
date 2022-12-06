package com.example.cmpt_362_chitchat.ui.login

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.*
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
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
import com.wajahatkarim3.easyvalidation.core.view_ktx.validEmail
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import java.time.Month

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var firstname: EditText
    private lateinit var lastname: EditText
    private lateinit var gender: RadioGroup
    private lateinit var selectedGender: RadioButton
    private lateinit var dob: DatePicker
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var username: EditText
    private lateinit var register: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        email = binding.registerEmail
        password = binding.registerPassword
        username = binding.registerUsername
        register = binding.registerBtn
        dob = binding.datePicker

        //val firstname = binding.registerfirstname
        firstname = binding.registerfirstname
        lastname = binding.registerlastname
        gender = binding.radioGroupGender


        register.setOnClickListener {
            //Validating user input
            if (email.validator().validEmail().addErrorCallback{ email.error = "Invalid email"}.check()
                && password.validator().nonEmpty().minLength(5).atleastOneUpperCase().atleastOneNumber().addErrorCallback { password.error = "At least 5 characters with 1 upper case and 1 number" }.check()
                && username.validator().nonEmpty().minLength(4).addErrorCallback { username.error = "At least 4 characters" }.check()
                && firstname.validator().nonEmpty().addErrorCallback { firstname.error = "Must not be empty" }.check()
                && lastname.validator().nonEmpty().addErrorCallback { lastname.error = "Must not be empty" }.check()
                && gender.checkedRadioButtonId != -1) {
                selectedGender = findViewById(gender.checkedRadioButtonId)
                val dob_string = "${Month.of(dob.month+1)}, ${dob.dayOfMonth}, ${dob.year}"
                println("DEBUG TEST DOB: $dob_string")
                if (username.text.toString() != "") {
                    //Creating account
                    addAccount(
                        this,
                        email.text.toString(),
                        password.text.toString(),
                        username.text.toString(),
                        firstname.text.toString(),
                        lastname.text.toString(),
                        selectedGender.text.toString(),
                        dob_string
                    )
                } else {
                    Toast.makeText(baseContext, "Username is required.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    //Adding account to database, initial information includes Email, Password, Username, Name and Gender
    fun addAccount(context: Context, email: String, password: String, username: String, firstname: String, lastname: String, gender: String, dob: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            if (it.isSuccessful){
                println("DEBUG REGISTER SUCCESS: email: $email, password: $password")
                auth.currentUser?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(username).build())
                addAccountToDatabase(auth.currentUser?.uid, email, username, firstname, lastname, gender, dob)
                auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                    Toast.makeText(this@RegisterActivity, "Verification email sent to $email", Toast.LENGTH_SHORT).show()
                }?.addOnFailureListener{
                    Toast.makeText(this@RegisterActivity, "Failed to send email verification to $email", Toast.LENGTH_SHORT).show()
                }
                auth.signOut()
                finish()
            } else {
                println("REGISTER FAIL")
            }
        }
    }


    private fun addAccountToDatabase(userId: String?, email: String, username: String, firstname: String, lastname: String, gender: String, dob: String) {
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
                .child("firstname")
                .setValue(firstname)
            database
                .child("Users")
                .child(userId)
                .child("lastname")
                .setValue(lastname)
            database
                .child("Users")
                .child(userId)
                .child("gender")
                .setValue(gender)
            database
                .child("Users")
                .child(userId)
                .child("DOB")
                .setValue(dob)
        } else {
            println("Debug: user not added to db correctly")
        }
    }
}

