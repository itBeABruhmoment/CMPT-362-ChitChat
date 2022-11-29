package com.example.cmpt_362_chitchat.ui.profile

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.ui.friends.FriendsActivity
import com.example.cmpt_362_chitchat.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import java.io.File
import java.util.*


class ProfileActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var profileItems: ListView
    private lateinit var photoDialog: Dialog


    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var userPhoto: ImageView
    private lateinit var userImageUri: Uri
    private lateinit var currentPhoto: File
    val GALLERY = 1

    private val calendar = Calendar.getInstance()

    private val profileDescription = arrayOf(
        "Username", "Name", "DOB", "Gender", "Password", "Email"
    )

    private var userInfo = arrayOf(
        "username", "Bob", "Feb 3, 2003", "Male", "*******", "email"
    )


    private lateinit var viewModel: ProfileViewModel
    private lateinit var profileAdapter : ProfileAdapter
    private lateinit var database: DatabaseReference
    private lateinit var user: FirebaseUser



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profileItems = findViewById(R.id.profileItems)

        //get access to viewModel for user profile
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        //get data from firebase
        user = FirebaseAuth.getInstance().currentUser!!
        // Name, email address, and profile photo Url
        val name = user?.displayName
        val email = user?.email
        val photoUrl = user?.photoUrl

        // Check if user's email is verified
        // The user's ID, unique to the Firebase project. Do NOT use this value to
        // authenticate with your backend server, if you have one. Use
        // FirebaseUser.getIdToken() instead.
        val uid = user?.uid
        println("DEBUG: name $name")
        println("DEBUG: uid $uid")
        println("DEBUG: email $email")
        println("DEBUG: photoUrl $photoUrl")

        //loads username
        if (uid != null) {
            database = FirebaseDatabase.getInstance().getReference("Users")
            database.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    //load username value
                    var username = it.child("username").value.toString()
                    println("DEBUG: username is $username")
                    //there is a delay for this method, so have to update adapter again (onStart code starts executing before this finish)
                    userInfo[0] = username
                    profileAdapter = ProfileAdapter(this, profileDescription, userInfo)
                    profileItems?.adapter = profileAdapter
                }
            }
        }

        if (email != null) {
            userInfo[5] = email
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_btn, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut()
            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onStart() {
        super.onStart()
        //setup list adapter for display
        user = FirebaseAuth.getInstance().currentUser!!
        profileAdapter = ProfileAdapter(this, profileDescription, userInfo)
        profileItems?.adapter = profileAdapter

        //Camera code
        userPhoto = findViewById(R.id.userPhoto)
        currentPhoto = File(getExternalFilesDir(null), "userPhoto_img.jpg")
        userImageUri = FileProvider.getUriForFile(this, "com.example.cmpt_362_chitchat", currentPhoto)
        cameraResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {

            }
        }

        //data not saved atm
        profileItems?.setOnItemClickListener(){adapterView, view, position, id ->
            val itemAtPos = adapterView.getItemAtPosition(position)

            when (itemAtPos) {
                "Username" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.USERNAME_DIALOG)
                    newDialog.arguments = bundle
                    newDialog.show(supportFragmentManager, "standard string")
                }
                "Name" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.NAME_DIALOG)
                    newDialog.arguments = bundle
                    newDialog.show(supportFragmentManager, "standard string")
                }
                "DOB" -> {
                    // change later
                    val datePickerDialog = DatePickerDialog(
                        this, this,calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.show()
                }
                "Gender" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.GENDER_DIALOG)
                    newDialog.arguments = bundle
                    newDialog.show(supportFragmentManager, "gender")
                }

                "Password" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.PASSWORD_DIALOG)
                    newDialog.arguments = bundle
                    newDialog.show(supportFragmentManager, "password")
                }

                "Email" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.EMAIL_DIALOG)
                    newDialog.arguments = bundle
                    newDialog.show(supportFragmentManager, "standard string")
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        println("DEBUG: RESUMED")
    }

    //dialog for selecting a new picture
    fun changePicture(view: View) {
        photoDialog = Dialog()
        val bundle = Bundle()
        bundle.putInt(Dialog.DIALOG_KEY, Dialog.PHOTO_DIALOG)
        photoDialog.arguments = bundle
        photoDialog.show(supportFragmentManager, "photo")
    }

    //open gallery for photo
    fun selectGallery(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY)
        photoDialog.dismiss()
    }

    //open camera for photo
    fun takePhoto(view: View) {
        //checks permission
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 0)
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, userImageUri)
        cameraResult.launch(intent)
        photoDialog.dismiss()
    }

    //switch to friend activity
    fun startFriendActivity(view: View) {
        val intent = Intent(this, FriendsActivity::class. java)
        startActivity(intent)
    }

    //
    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {

    }


    //for updating user data
    fun saveUserData(view: View) {
        //get dialog info
        var dialogID = viewModel.getDialogID()
        var dialog = viewModel.getDialog()

        //firebase connection
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            //email
            if (dialogID == 7) {
                // get current input text
                var emailEditText = dialog.findViewById<EditText>(R.id.Edit)
                var emailString = emailEditText.text.toString()

                //checking if email is valid
                if (!TextUtils.isEmpty(emailString) && Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
                    //update email info
                    user.updateEmail(emailString)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("DEBUG: EMAIL UPDATED")
                                userInfo[5] = emailString
                                //update view for adapter
                                profileAdapter = ProfileAdapter(this, profileDescription, userInfo)
                                profileAdapter.notifyDataSetChanged()
                                profileItems.adapter = profileAdapter
                                //let user know email updated and dismiss dialog
                                Toast.makeText(applicationContext,"Email successfully updated",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                println("DEBUG: EMAIL DID NOT UPDATE")
                            }
                        }
                } else {
                    Toast.makeText(applicationContext,"Invalid email",Toast.LENGTH_SHORT).show()
                }
            } else if (dialogID == 3) { //Password
                var newPass = dialog.findViewById<EditText>(R.id.password)
                var cnewPass = dialog.findViewById<EditText>(R.id.confirmPassword)
                var newPassString = newPass.text.toString()
                var newcPassString = cnewPass.text.toString()

                //check if new pass is acceptable
                if (newPassString == newcPassString && newPassString.length > 5) {
                    user.updatePassword(newPassString)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                println("DEBUG: Pass updated")
                                //let user know email updated and dismiss dialog
                                Toast.makeText(applicationContext,"Password successfully updated",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                println("DEBUG: Pass fail to update")
                            }
                        }
                } else {
                    Toast.makeText(applicationContext,"Invalid length or password do not match",Toast.LENGTH_SHORT).show()
                }
            } else if (dialogID == 4) { //username
                var username = dialog.findViewById<EditText>(R.id.Edit)
                var usernameString = username.text.toString()


                if (usernameString.length > 5) {
                    //update username
                    var uid = user.uid
                    if (uid != null) {
                        database = FirebaseDatabase.getInstance().getReference("Users")
                        database.child(uid).child("username").setValue(usernameString)
                        userInfo[0] = usernameString
                        //update view for adapter
                        profileAdapter = ProfileAdapter(this, profileDescription, userInfo)
                        profileAdapter.notifyDataSetChanged()
                        profileItems.adapter = profileAdapter

                        //dismiss dialog and let user know
                        Toast.makeText(applicationContext,"username successfully updated",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(applicationContext,"username was not updated",Toast.LENGTH_SHORT).show()
                }


            }
        } else {
            println("DEBUG: user is null (SHOULD NEVER HAPPEN)")
        }
    }

    //cancel for dialog
    fun cancelButton(view: View) {
        //get dialog info
        var dialog = viewModel.getDialog()
        dialog.dismiss()
    }

}