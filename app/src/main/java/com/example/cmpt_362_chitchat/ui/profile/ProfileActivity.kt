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
import com.google.firebase.auth.FirebaseAuth
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

    var userInfo = arrayOf(
        "usernamePlaceHolder", "namePlacerHolder", "DOBPlacerHolder", "genderPlaceHolder", "passwordPlaceHolder", "emailPlaceHolder"
    )

    private lateinit var viewModel: ProfileViewModel

    private lateinit var profileAdapter : ProfileAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profileItems = findViewById(R.id.profileItems)

        //get access to viewModel for user profile
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)


        //connect to firebase
        val user = FirebaseAuth.getInstance().currentUser

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


        if (email != null) {
            userInfo[5] = email
        }
        //setup list adapter for display
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
            println("DEBUG: $itemAtPos")

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

            var value = profileAdapter.getItem(2)
            println("DEBUG HEEEEEEEEEEEEEEE $value")
            //email
            if (dialogID == 7) {
                // get current input text
                var emailEditText = dialog.findViewById<EditText>(R.id.Edit)
                val emailString = emailEditText.text.toString()

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
                            } else {
                                println("DEBUG: EMAIL DID NOT UPDATE")
                            }
                        }
                    //let user know email updated and dismiss dialog
                    Toast.makeText(applicationContext,"Email successfully updated",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(applicationContext,"Invalid email",Toast.LENGTH_SHORT).show()
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