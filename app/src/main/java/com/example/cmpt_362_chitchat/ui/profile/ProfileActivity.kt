package com.example.cmpt_362_chitchat.ui.profile

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.cmpt_362_chitchat.R
import com.example.cmpt_362_chitchat.ui.friends.FriendsActivity
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
        "Username", "Name", "DOB", "Gender", "Password"
    )

    //replace with database later
    private val placeHolderDatabaseInfo = arrayOf(
        "usernamePlaceHolder", "namePlacerHolder", "DOBPlacerHolder", "genderPlaceHolder", "passwordPlaceHolder"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        profileItems = findViewById(R.id.profileItems)

        val profileAdapter = ProfileAdapter(this, profileDescription, placeHolderDatabaseInfo)
        profileItems?.adapter = profileAdapter


        //currently using lecture code
        userPhoto = findViewById(R.id.userPhoto)
        currentPhoto = File(getExternalFilesDir(null), "userPhoto_img.jpg")
        userImageUri = FileProvider.getUriForFile(this, "com.example.cmpt_362_chitchat", currentPhoto)
        cameraResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {

            }
        }
        //


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
            }
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


}