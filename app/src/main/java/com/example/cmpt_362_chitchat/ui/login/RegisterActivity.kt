package com.example.cmpt_362_chitchat.ui.login

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var accountManager: AccountManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.registerusername
        val password = binding.registerpassword
        val register = binding.registerBtn

        auth = Firebase.auth

        register.setOnClickListener {
            addAccount(this, username.text.toString(), password.text.toString(), "123")
           // getAccount(this)

        }

    }

    fun addAccount(context: Context, username: String, password: String, token: String) {

        //add confirm pw?

        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this){
            if (it.isSuccessful){
                println("DEBUG REGISTER SUCCESS: username: $username, password: $password")
                finish()
            }else{
                println("REGISTER FAIL")
            }
        }
    }

    fun getAccount(context: Context): Account?{
        accountManager = AccountManager.get(context)
        var acc: Account? = null
        try {
            acc = accountManager.getAccountsByType("com.login.example")[0]
            println("DEBUG: USERNAME: ${acc.name}")

        }catch (E: Throwable){

        }
        return acc
    }
}