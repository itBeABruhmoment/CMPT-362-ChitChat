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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.Month
import java.util.*

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
    private lateinit var passwordConfirm: EditText
    private lateinit var username: EditText
    private lateinit var register: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle("Register a new Account")
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        email = binding.registerEmail
        password = binding.registerPassword
        passwordConfirm = binding.registerPasswordConfirm
        username = binding.registerUsername
        register = binding.registerBtn
        dob = binding.datePicker

        firstname = binding.registerfirstname
        lastname = binding.registerlastname
        gender = binding.radioGroupGender


        register.setOnClickListener {

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (password.text.toString() != passwordConfirm.text.toString()){
                passwordConfirm.error = "Passwords do not match"
            }
            if (currentYear - dob.year < 16){
                Toast.makeText(this, "Invalid date (too young)!", Toast.LENGTH_SHORT).show()
            }
            /* Validating User Input
             Checks Email, password, username and gender
             */
            if (username.validator().nonEmpty().minLength(4).addErrorCallback { username.error = "At least 4 characters" }.check()
                && email.validator().validEmail().addErrorCallback{ email.error = "Invalid email"}.check()
                && password.validator().nonEmpty().minLength(5).atleastOneUpperCase().atleastOneNumber().addErrorCallback { password.error = "At least 5 characters with 1 upper case and 1 number" }.check()
                && password.text.toString() == passwordConfirm.text.toString()
                && gender.checkedRadioButtonId != -1
                && currentYear - dob.year >= 16
            ) {

                // If First and Last name is left empty. Set it to an Empty String to prevent it from showing as Null in ProfileActivity
                if (!!firstname.nonEmpty()){
                    firstname.setText("")
                }
                if (!!lastname.nonEmpty()!!){
                    lastname.setText("")
                }
                selectedGender = findViewById(gender.checkedRadioButtonId)
                val dob_string = "${Month.of(dob.month+1)}, ${dob.dayOfMonth}, ${dob.year}"
                if (username.text.toString() != "") {
                    //Creating account with given user inputs
                    addAccount(
                        this@RegisterActivity,
                        email.text.toString(),
                        password.text.toString(),
                        username.text.toString(),
                        firstname.text.toString(),
                        lastname.text.toString(),
                        selectedGender.text.toString(),
                        dob_string
                    )
                }
            }

        }
    }

    //Adding account to database, initial information includes Email, Password, Username, Name and Gender
    fun addAccount(context: Context, email: String, password: String, username: String, firstname: String, lastname: String, gender: String, dob: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            if (it.isSuccessful){
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
                Toast.makeText(this, "An account with the email already exists. Please try a different email address", Toast.LENGTH_SHORT).show()
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
        }
    }
}

