package com.fivempk.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.fivempk.R
import com.fivempk.databinding.ActivityUserProfileBinding
import com.fivempk.firebase.FireBaseClass
import com.fivempk.models.User
import com.fivempk.utils.Constants
import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts

class UserProfileActivity : AppCompatActivity() {
    private var binding: ActivityUserProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private var mSelectedImageFileUri : Uri? = null
    private var mProfileImageURL :String = ""
    private lateinit var mUserDetails:User

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri here
            if (uri != null) {
                mSelectedImageFileUri = uri
                try {
                    Glide
                        .with(this)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.background_witg_vector)
                        .into(binding!!.userImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    companion object{
        private const val READ_STORAGE_PERMISSION_CODE = 1
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth= FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            FireBaseClass().signInUser(this)
        }else{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding?.userImage?.setOnClickListener {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkPermissionsTiramisu() -> {
                    showImageChooser()
                }
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    showImageChooser()
                }
                else -> {
                    requestStoragePermission()
                }
            }
        }

        binding!!.deleteBtn.setOnClickListener {
            currentUser!!.delete()
            FireBaseClass().deleteUser(this)
            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        }

        binding!!.UpdateBtn.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadUserImage()
            }else{
                val btn : CircularProgressButton = findViewById(R.id.Update_btn)
                btn.startAnimation()
                updateUserProfileData()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionsTiramisu(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            READ_STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImageChooser()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showImageChooser() {
        getContent.launch("image/*")
    }

    fun fillUserInfo(user:User){
        mUserDetails = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.background_witg_vector)
            .into(binding!!.userImage)
        binding!!.edittextName.setText(user.name)
        binding!!.edittextEmail.setText(user.email)
        binding!!.edittextPhone.setText(user.phone)
    }

    private fun updateUserProfileData(){
        val  userHashMap = HashMap<String,Any>()
         if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
             userHashMap[Constants.IMAGE] = mProfileImageURL
         }
        FireBaseClass().updateUserProfileData(this , userHashMap)
    }

    private fun uploadUserImage(){
        val btn : CircularProgressButton = findViewById(R.id.Update_btn)
        btn.startAnimation()
        if(mSelectedImageFileUri != null){
            val sRef:StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "USER_IMAGE"+System.currentTimeMillis()
                            + "." + getFileExtension(mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot -> Log.i(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image URL",uri.toString())
                    mProfileImageURL = uri.toString()
                    btn.revertAnimation()
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                btn.revertAnimation()
            }
        }
    }

    private fun getFileExtension(uri:Uri?):String?{
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        val btn : CircularProgressButton = findViewById(R.id.Update_btn)
        btn.revertAnimation()
        finish()
    }
}