package com.fivempk.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.maps.android.PackageManager
import java.io.IOException

class UserProfileActivity : AppCompatActivity() {
    private var binding: ActivityUserProfileBinding? = null
    private lateinit var auth: FirebaseAuth
    private var mSelectedImageFileUri : Uri? = null
    private var mProfileImageURL :String = ""
    private lateinit var mUserDetails:User

    companion object{
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
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

        binding!!.userImage.setOnClickListener{
            if (ContextCompat.checkSelfPermission(
                    this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED){
                showImageChooser()

            }else{
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE)
            }
        }

        binding!!.deleteBtn.setOnClickListener {
            currentUser!!.delete()
            FireBaseClass().deleteUser(this)
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, handle the case
                showImageChooser()
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied, handle the case
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showImageChooser(){
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent,PICK_IMAGE_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null ){
            mSelectedImageFileUri = data.data


            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.background_witg_vector)
                    .into(binding!!.userImage)
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
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