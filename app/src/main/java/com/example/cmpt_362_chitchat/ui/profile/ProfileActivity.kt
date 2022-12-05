package com.example.cmpt_362_chitchat.ui.profile

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.*


class ProfileActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    //camera stuff
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var userPhoto: ImageView
    private lateinit var userImageUri: Uri
    private lateinit var photoFile: File
    companion object {
        const val GALLERY = 1
    }

    private lateinit var profileItems: ListView
    private lateinit var photoDialog: Dialog

    private val calendar = Calendar.getInstance()

    private val profileDescription = arrayOf(
        "Username", "First name", "Last name", "DOB", "Gender", "Password", "Email"
    )

    private var userInfo = arrayOf(
        "username", "Bob", "GoodWill", "Feb 3, 2003", "Male", "*******", "email"
    )

    private lateinit var viewModel: ProfileViewModel
    private lateinit var profileAdapter : ProfileAdapter
    private lateinit var database: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var auth: FirebaseAuth

    private lateinit var storageReference : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profileItems = findViewById(R.id.profileItems)

        //get access to viewModel for user profile
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        //firebase auth
        auth = Firebase.auth

        //get data from firebase
        user = FirebaseAuth.getInstance().currentUser!!
        // Name, email address, and profile photo Url
        val email = user.email
        val uid = user.uid

        //change username, DOB, gender, and name placeholder (use firebase username)
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                //load username value
                val username = it.child("username").value.toString()
                val firstName = it.child("firstName").value.toString()
                val lastName = it.child("lastName").value.toString()
                val gender = it.child("gender").value.toString()
                val dateOfBirth = it.child("DOB").value.toString()
                //there is a delay for this method, so have to update adapter again (onStart code starts executing before this finish)
                userInfo[0] = username
                userInfo[1] = firstName
                userInfo[2] = lastName
                userInfo[3] = dateOfBirth
                userInfo[4] = gender
                profileAdapter = ProfileAdapter(this, profileDescription, userInfo)
                profileItems.adapter = profileAdapter
            }
        }

        //change email placeholder (use firebase email)
        if (email != null) {
            userInfo[6] = email
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
            val intent = Intent(this, LoginActivity::class.java)
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
        profileItems.adapter = profileAdapter

        //load user photo from database
        val uid = user.uid
        loadPhoto(uid)

        //Camera code from lecture
        userPhoto = findViewById(R.id.userPhoto)
        photoFile = File(getExternalFilesDir(null), "userPhoto_img.jpg")
        userImageUri = FileProvider.getUriForFile(this, "com.example.cmpt_362_chitchat", photoFile)

        //camera photo success
        cameraResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // get uid
                uploadPhoto(uid) // upload photo to database storage based on user id
            }
        }

        //work in progress
        profileItems.setOnItemClickListener { adapterView, _, position, _ ->
            when (adapterView.getItemAtPosition(position)) {
                "Username" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.USERNAME_DIALOG)
                    newDialog.arguments = bundle
                    newDialog.show(supportFragmentManager, "standard string")
                }
                "First name" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.NAME_DIALOG)
                    newDialog.arguments = bundle
                    newDialog.show(supportFragmentManager, "standard string")
                }

                "Last name" -> {
                    val newDialog  = Dialog()
                    val bundle = Bundle()
                    bundle.putInt(Dialog.DIALOG_KEY, Dialog.NAME_LAST_DIALOG)
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

    //load photo from database
    private fun loadPhoto(uid : String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("UserPhotos/$uid")
        //create a temp location for photo
        val localFile = File.createTempFile("tempImage", "jpg")
        storageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            //update userPhoto
            userPhoto.setImageBitmap(bitmap)
            println("DEBUG: photo successfully loaded")
        }.addOnFailureListener {
            println("DEBUG: photo was not able to load")
        }
    }

    //upload image to database
    private fun uploadPhoto(uid : String) {
        // safety check
        //add image to specify firebase storage location
        storageReference = FirebaseStorage.getInstance().getReference("UserPhotos/$uid")
        storageReference.putFile(userImageUri).addOnSuccessListener {
            Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show()
            loadPhoto(uid)
        }.addOnFailureListener {
            Toast.makeText(this, "Photo fail to save", Toast.LENGTH_SHORT).show()
        }
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

    //gallery photo request
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY && resultCode == RESULT_OK) {
            //display the photo that was selected, could also call database, but inefficient because takes up more time
            userImageUri = data?.data!!
          //  userPhoto.setImageURI(data?.data)
            // get uid
            val uid = user.uid
            uploadPhoto(uid) // upload photo to database storage based on user id
        }
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

    //updates DOB
    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // has to be 16 years old to use app
        if (currentYear - year < 16) {
            Toast.makeText(applicationContext,"Invalid date (too young)!",Toast.LENGTH_SHORT).show()
        } else {
            val uid = user.uid
            val month = getMonth(monthOfYear)
            val dateString = "$month $dayOfMonth $year"
            database = FirebaseDatabase.getInstance().getReference("Users")
            database.child(uid).child("DOB").setValue(dateString)
            Toast.makeText(applicationContext,"DOB successfully updated",Toast.LENGTH_SHORT).show()
            userInfo[3] = dateString

            //update view for adapter
            profileAdapter = ProfileAdapter(this, profileDescription, userInfo)
            profileAdapter.notifyDataSetChanged()
            profileItems.adapter = profileAdapter
        }

    }


    //for updating user data
    fun saveUserData(view: View) {
        user = FirebaseAuth.getInstance().currentUser!!
        //get dialog info
        val dialogID = viewModel.getDialogID()
        val dialog = viewModel.getDialog()

        //get user id
        val uid = user.uid
        //firebase connection
        if (user != null) {
            //email
            if (dialogID == 7) {
                // get current input text
                val emailEditText = dialog.findViewById<EditText>(R.id.Edit)
                val emailString = emailEditText.text.toString()

                //checking if email is valid
                if (!TextUtils.isEmpty(emailString) && Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
                    //update email info
                    user.updateEmail(emailString)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                userInfo[6] = emailString

                                //update the firebase database
                                database = FirebaseDatabase.getInstance().getReference("Users")
                                database.child(uid).child("email").setValue(emailString)

                                //let user know email updated and dismiss dialog
                                Toast.makeText(applicationContext,"Email successfully updated",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()

                                //send email verification
                                auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                                    Toast.makeText(this@ProfileActivity, "Verification email sent to ...", Toast.LENGTH_SHORT).show()
                                }?.addOnFailureListener{
                                    Toast.makeText(this@ProfileActivity, "Failed to send email verification to ..", Toast.LENGTH_SHORT).show()
                                }

                                //signout after changing email
                                auth.signOut()
                                intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                //assuming, double check alter
                                Toast.makeText(applicationContext,"Email already in user. please select another email.",Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(applicationContext,"Invalid email",Toast.LENGTH_SHORT).show()
                }
            } else if (dialogID == 3) { //Password
                val newPass = dialog.findViewById<EditText>(R.id.password)
                val cnewPass = dialog.findViewById<EditText>(R.id.confirmPassword)
                val newPassString = newPass.text.toString()
                val newcPassString = cnewPass.text.toString()

                //check if new pass is acceptable
                if (newPassString == newcPassString && newPassString.length > 5) {
                    user.updatePassword(newPassString)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //let user know email updated and dismiss dialog
                                Toast.makeText(applicationContext,"Password successfully updated",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(applicationContext,"Firebase failed to update password",Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(applicationContext,"Invalid length or password do not match",Toast.LENGTH_SHORT).show()
                }
            } else if (dialogID == 4) { //username
                val username = dialog.findViewById<EditText>(R.id.Edit)
                val usernameString = username.text.toString()

                if (usernameString.length > 5) {
                    //update username
                    database = FirebaseDatabase.getInstance().getReference("Users")
                    database.child(uid).child("username").setValue(usernameString)
                    userInfo[0] = usernameString
                    //dismiss dialog and let user know
                    Toast.makeText(applicationContext,"username successfully updated",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(applicationContext, "username is too short", Toast.LENGTH_SHORT).show()
                }
            } else if (dialogID == 5 ) { // first name
                val name = dialog.findViewById<EditText>(R.id.Edit)
                val nameString = name.text.toString()
                //no required check for name
                database = FirebaseDatabase.getInstance().getReference("Users")
                database.child(uid).child("firstName").setValue(nameString)
                userInfo[1] = nameString
                //dismiss dialog and let user know update
                Toast.makeText(applicationContext,"first name successfully updated",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else if (dialogID == 2) { //gender
                //determine what gender got selected from viewModel
                val genderSelected = viewModel.returnGender()
                //if user selects an option update to database otherwise do nothing
                if (genderSelected != "") {
                    database = FirebaseDatabase.getInstance().getReference("Users")
                    database.child(uid).child("gender").setValue(genderSelected)
                    userInfo[4] = genderSelected
                    //dismiss dialog and let user know update
                    Toast.makeText(applicationContext,"gender updated successfully",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(applicationContext,"Please select a gender",Toast.LENGTH_SHORT).show()
                }
                viewModel.setGender("") //clean viewModel for gender
            } else if (dialogID == 8) { // last name
                val name = dialog.findViewById<EditText>(R.id.Edit)
                val nameString = name.text.toString()
                //no required check for name
                database = FirebaseDatabase.getInstance().getReference("Users")
                database.child(uid).child("lastName").setValue(nameString)
                userInfo[2] = nameString
                //dismiss dialog and let user know update
                Toast.makeText(applicationContext,"last name successfully updated",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                //Note this sometimes happen, no clue as to why
                println("DEBUG: user is null (SHOULD NEVER HAPPEN)")
            }
            //update view for adapter
            profileAdapter = ProfileAdapter(this, profileDescription, userInfo)
            profileAdapter.notifyDataSetChanged()
            profileItems.adapter = profileAdapter
        }
    }

    //cancel for dialog
    fun cancelButton(view: View) {
        //get dialog info
        val dialog = viewModel.getDialog()
        dialog.dismiss()
    }

    //convert int to string for months
    private fun getMonth(monthOfYear: Int) : String {
        var month = ""
        when (monthOfYear) {
            0 -> {
                month = "Jan"
            }
            1 -> {
                month = "Feb"
            }
            2 -> {
                month = "Mar"
            }
            3 -> {
                month = "Apr"
            }
            4 -> {
                month = "May"
            }
            5 -> {
                month = "June"
            }
            6 -> {
                month = "July"
            }
            7 -> {
                month = "Aug"
            }
            8 -> {
                month = "Sept"
            }
            9 -> {
                month = "Oct"
            }
            10 -> {
                month = "Nov"
            }
            11 -> {
                month = "Dec"
            }
        }
        return month
    }

}