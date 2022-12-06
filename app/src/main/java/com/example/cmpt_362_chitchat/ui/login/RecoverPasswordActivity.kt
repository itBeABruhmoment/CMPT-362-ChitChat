package com.example.cmpt_362_chitchat.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.cmpt_362_chitchat.R
import com.google.firebase.auth.FirebaseAuth

class RecoverPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var sendbtn: Button
    private lateinit var cancelbtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        auth = FirebaseAuth.getInstance()

        email = findViewById(R.id.EditResetPasswordEmail)
        sendbtn = findViewById(R.id.sendBtn)
        cancelbtn = findViewById(R.id.cancelBtn)

        sendbtn.setOnClickListener {
            auth.sendPasswordResetEmail(email.text.toString())
            Toast.makeText(this@RecoverPasswordActivity, "Successfully sent reset link to ${email.text}", Toast.LENGTH_SHORT).show()
        }

        cancelbtn.setOnClickListener{
            finish()
        }

    }
}