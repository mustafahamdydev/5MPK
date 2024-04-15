package com.fivempk.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.fivempk.R
import com.fivempk.databinding.ActivityUserProfileBinding
import com.fivempk.firebase.FireBaseClass
import com.fivempk.models.User
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : AppCompatActivity() {
    private var binding: ActivityUserProfileBinding? = null
    private lateinit var auth: FirebaseAuth
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

        binding!!.deleteBtn.setOnClickListener {
            currentUser!!.delete()
            FireBaseClass().deleteUser(this)
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }



    }
    fun fillUserInfo(user:User){
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
}