package com.example.cmpt_362_chitchat.data

import android.accounts.AccountManager
import com.example.cmpt_362_chitchat.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 * -David:
 * Looking to use AccountManager to implement this
 */
class LoginDataSource {

    private lateinit var accountManager: AccountManager
    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            return Result.Success(fakeUser)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}