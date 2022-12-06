package com.example.cmpt_362_chitchat.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.cmpt_362_chitchat.databinding.ActivityLoginBinding
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.ui.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val register = binding.registerNewUser
        val forgot = binding.textViewForgot

        auth = FirebaseAuth.getInstance()

        register?.setOnClickListener {
            intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgot!!.setOnClickListener{
            intent = Intent(this, RecoverPasswordActivity::class.java)
            startActivity(intent)
        }

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                email.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {

                auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString()).addOnCompleteListener(this) {
                    /*Email Verification
                    If email is not verified, cannot login. However this would be annoying to have enabled while we are testing the app so it will be disabled
                    until the app is finished
                     */
                    if (it.isSuccessful){// && auth.currentUser?.isEmailVerified == true){
                        Toast.makeText(this, "Login Successful! Welcome!", Toast.LENGTH_SHORT).show()
                        intent = Intent(this, HomeActivity::class.java)
                        setResult(Activity.RESULT_OK)
                        startActivity(intent)
                        finish()
                    }else if(it.isSuccessful && auth.currentUser!!.isEmailVerified != true) {
                        Toast.makeText(this, "Email has not yet been verified. Please verify your email address and try again", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Failed to Login. Please check your email and password and try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            setResult(Activity.RESULT_OK)
        })

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            email.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(email.text.toString(), password.text.toString())
            }
        }
    }

    override fun onResume() {
        if (Firebase.auth.currentUser != null){
            intent = Intent(this, HomeActivity::class.java)
            setResult(Activity.RESULT_OK)
            startActivity(intent)
            finish()
        }
        super.onResume()
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
